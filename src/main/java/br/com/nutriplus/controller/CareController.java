package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.AcceptInviteRequest;
import br.com.nutriplus.dto.request.CareRatingRequest;
import br.com.nutriplus.dto.request.CareRequestRequest;
import br.com.nutriplus.dto.request.ConsultationPayRequest;
import br.com.nutriplus.dto.response.RegisterResponse;
import br.com.nutriplus.dto.response.CareContactResponse;
import br.com.nutriplus.dto.response.CareRatingResponse;
import br.com.nutriplus.dto.response.CareRelationshipResponse;
import br.com.nutriplus.dto.response.PaymentIntentResponse;
import br.com.nutriplus.dto.request.NutritionistRegisterRequest;
import br.com.nutriplus.service.CareRatingService;
import br.com.nutriplus.service.CareService;
import br.com.nutriplus.service.NutritionistProService;
import br.com.nutriplus.service.StripePaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CareController {

    private final CareService careService;
    private final StripePaymentService stripePaymentService;
    private final NutritionistProService nutritionistProService;
    private final CareRatingService careRatingService;

    public CareController(CareService careService,
                          StripePaymentService stripePaymentService,
                          NutritionistProService nutritionistProService,
                          CareRatingService careRatingService) {
        this.careService = careService;
        this.stripePaymentService = stripePaymentService;
        this.nutritionistProService = nutritionistProService;
        this.careRatingService = careRatingService;
    }

    @PostMapping("/auth/register/nutritionist")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerNutritionist(@Valid @RequestBody NutritionistRegisterRequest request) {
        return nutritionistProService.register(request);
    }

    @PostMapping("/care/accept-invite/{code}")
    public CareRelationshipResponse acceptInvite(@PathVariable String code,
                                                 @Valid @RequestBody AcceptInviteRequest request) {
        return careService.acceptInvite(code, request);
    }

    @PostMapping("/care/request/{nutritionistId}")
    public CareRelationshipResponse requestCare(@PathVariable Long nutritionistId,
                                                @Valid @RequestBody CareRequestRequest request) {
        return careService.requestMarketplaceCare(nutritionistId, request);
    }

    @PostMapping("/care/relationships/{careRelationshipId}/rate")
    public CareRatingResponse rate(@PathVariable Long careRelationshipId,
                                   @Valid @RequestBody CareRatingRequest request) {
        return careRatingService.rate(careRelationshipId, request);
    }

    @GetMapping("/care/relationships/{careRelationshipId}/contact")
    public CareContactResponse careContact(@PathVariable Long careRelationshipId) {
        return careService.getCareContact(careRelationshipId);
    }

    @GetMapping("/care/my")
    public List<CareRelationshipResponse> myCare() {
        return careService.listMyCareAsPatient();
    }

    @PostMapping("/consultations/pay")
    public PaymentIntentResponse pay(@Valid @RequestBody ConsultationPayRequest request) {
        return stripePaymentService.createConsultationPayment(request);
    }
}
