package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.ConsultationStatus;
import br.com.nutriplus.dto.request.ConsultationPayRequest;
import br.com.nutriplus.dto.response.PaymentIntentResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.infrastructure.config.StripeProperties;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.ConsultationRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.StripeCustomerRepository;
import br.com.nutriplus.security.AuthorizationService;
import br.com.nutriplus.security.CurrentUser;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

    private final StripeProperties stripeProperties;
    private final CurrentUser currentUser;
    private final CareRelationshipRepository careRelationshipRepository;
    private final ConsultationRepository consultationRepository;
    private final StripeCustomerRepository stripeCustomerRepository;
    private final CareService careService;
    private final PricingGuidelineService pricingGuidelineService;
    private final AuthorizationService authorizationService;
    private final NutritionistRepository nutritionistRepository;

    public StripePaymentService(StripeProperties stripeProperties,
                                CurrentUser currentUser,
                                CareRelationshipRepository careRelationshipRepository,
                                ConsultationRepository consultationRepository,
                                StripeCustomerRepository stripeCustomerRepository,
                                CareService careService,
                                PricingGuidelineService pricingGuidelineService,
                                AuthorizationService authorizationService,
                                NutritionistRepository nutritionistRepository) {
        this.stripeProperties = stripeProperties;
        this.currentUser = currentUser;
        this.careRelationshipRepository = careRelationshipRepository;
        this.consultationRepository = consultationRepository;
        this.stripeCustomerRepository = stripeCustomerRepository;
        this.careService = careService;
        this.pricingGuidelineService = pricingGuidelineService;
        this.authorizationService = authorizationService;
        this.nutritionistRepository = nutritionistRepository;
    }

    @PostConstruct
    void init() {
        if (stripeProperties.isConfigured()) {
            Stripe.apiKey = stripeProperties.secretKey();
        }
    }

    public boolean isMockMode() {
        return stripeProperties.mockMode() || !stripeProperties.isConfigured();
    }

    @Transactional
    public PaymentIntentResponse createConsultationPayment(ConsultationPayRequest request) {
        User patient = currentUser.get();
        Nutritionist nutritionist = careService.getMarketplaceNutritionist(request.nutritionistId());

        CareRelationship care = careRelationshipRepository
                .findByPatientIdAndNutritionistId(patient.getId(), nutritionist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicite o vínculo antes de pagar."));

        if (care.getStatus() == CareRelationshipStatus.ACTIVE) {
            throw new BusinessException("Acompanhamento já está ativo.");
        }

        int amount = nutritionist.getConsultationPriceCents();
        int platformFee = pricingGuidelineService.calculatePlatformFee(amount);

        Consultation consultation = Consultation.create(care, amount, platformFee);
        consultation = consultationRepository.save(consultation);

        if (isMockMode()) {
            String mockId = "mock_pi_" + UUID.randomUUID();
            consultation.setStripePaymentIntentId(mockId);
            consultationRepository.save(consultation);
            careService.activateCareAfterPayment(care, consultation);
            consultationRepository.save(consultation);
            return new PaymentIntentResponse(null, mockId, amount, null, true);
        }

        if (!nutritionist.isStripeOnboardingComplete()) {
            throw new BusinessException("Nutricionista ainda não configurou recebimentos.");
        }

        try {
            String customerId = ensureStripeCustomer(patient);
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) amount)
                    .setCurrency("brl")
                    .setCustomer(customerId)
                    .putMetadata("consultation_id", consultation.getId().toString())
                    .putMetadata("care_relationship_id", care.getId().toString())
                    .setApplicationFeeAmount((long) platformFee)
                    .setTransferData(PaymentIntentCreateParams.TransferData.builder()
                            .setDestination(nutritionist.getStripeAccountId())
                            .build())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            consultation.setStripePaymentIntentId(intent.getId());
            consultationRepository.save(consultation);

            return new PaymentIntentResponse(intent.getClientSecret(), intent.getId(), amount, null, false);
        } catch (StripeException e) {
            log.error("Stripe payment error", e);
            throw new BusinessException("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        if (isMockMode()) {
            return;
        }
        try {
            var event = Webhook.constructEvent(payload, sigHeader, stripeProperties.webhookSecret());
            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intent != null) {
                    confirmPayment(intent.getId());
                }
            }
        } catch (Exception e) {
            log.error("Webhook error", e);
            throw new BusinessException("Webhook inválido");
        }
    }

    @Transactional
    public void confirmPayment(String paymentIntentId) {
        Consultation consultation = consultationRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada"));
        if (consultation.getStatus() == ConsultationStatus.PAID) {
            return;
        }
        CareRelationship care = consultation.getCareRelationship();
        careService.activateCareAfterPayment(care, consultation);
        consultationRepository.save(consultation);
    }

    @Transactional
    public br.com.nutriplus.dto.response.StripeConnectResponse startConnectOnboarding() {
        Nutritionist nutritionist = authorizationService.requireNutritionist();
        if (isMockMode()) {
            nutritionist.setStripeAccountId("acct_mock_" + nutritionist.getId());
            nutritionist.setStripeOnboardingComplete(true);
            nutritionistRepository.save(nutritionist);
            return new br.com.nutriplus.dto.response.StripeConnectResponse(null, true, nutritionist.getStripeAccountId());
        }
        try {
            if (nutritionist.getStripeAccountId() == null) {
                Account account = Account.create(AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setCountry("BR")
                        .setEmail(nutritionist.getUser().getEmail())
                        .setCapabilities(AccountCreateParams.Capabilities.builder()
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                                .build())
                        .build());
                nutritionist.setStripeAccountId(account.getId());
            }
            AccountLink link = AccountLink.create(AccountLinkCreateParams.builder()
                    .setAccount(nutritionist.getStripeAccountId())
                    .setRefreshUrl(stripeProperties.connectRefreshUrl())
                    .setReturnUrl(stripeProperties.connectReturnUrl())
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build());
            return new br.com.nutriplus.dto.response.StripeConnectResponse(
                    link.getUrl(), nutritionist.isStripeOnboardingComplete(), nutritionist.getStripeAccountId());
        } catch (StripeException e) {
            throw new BusinessException("Erro Stripe Connect: " + e.getMessage());
        }
    }

    private String ensureStripeCustomer(User patient) throws StripeException {
        return stripeCustomerRepository.findByUserId(patient.getId())
                .map(StripeCustomer::getStripeCustomerId)
                .orElseGet(() -> {
                    try {
                        Customer customer = Customer.create(CustomerCreateParams.builder()
                                .setEmail(patient.getEmail())
                                .setName(patient.getName())
                                .putMetadata("user_id", patient.getId().toString())
                                .build());
                        stripeCustomerRepository.save(StripeCustomer.of(patient, customer.getId()));
                        return customer.getId();
                    } catch (StripeException e) {
                        throw new BusinessException("Erro ao criar customer Stripe");
                    }
                });
    }
}
