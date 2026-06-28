package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.SubscriptionPlanCatalog;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.UpdateSubscriptionPlanRequest;
import br.com.nutriplus.dto.response.AdminSubscriptionPlanResponse;
import br.com.nutriplus.dto.response.PlanCatalogItemResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.SubscriptionPlanCatalogRepository;
import br.com.nutriplus.security.AuthorizationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class SubscriptionPlanCatalogService {

    private final SubscriptionPlanCatalogRepository repository;
    private final ObjectMapper objectMapper;
    private final AuthorizationService authorizationService;

    public SubscriptionPlanCatalogService(SubscriptionPlanCatalogRepository repository,
                                          ObjectMapper objectMapper,
                                          AuthorizationService authorizationService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.authorizationService = authorizationService;
    }

    public List<PlanCatalogItemResponse> catalogoPublico() {
        return repository.findByEnabledTrueAndVisibleInCatalogTrueOrderBySortOrderAscPlanCodeAsc().stream()
                .map(this::toCatalogItem)
                .toList();
    }

    public List<AdminSubscriptionPlanResponse> listForAdmin() {
        requireAdmin();
        return repository.findAllByOrderBySortOrderAscPlanCodeAsc().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public AdminSubscriptionPlanResponse update(Long id, UpdateSubscriptionPlanRequest request) {
        requireAdmin();
        SubscriptionPlanCatalog plan = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado."));

        if (plan.getPlanCode() == SubscriptionPlan.FREE && request.priceCents() != 0) {
            throw new BusinessException("O plano gratuito deve permanecer com preço zero.");
        }
        if (plan.getPlanCode() != SubscriptionPlan.FREE && request.priceCents() < 100) {
            throw new BusinessException("Planos pagos devem ter valor mínimo de R$ 1,00.");
        }

        plan.setName(request.name().trim());
        plan.setDescription(trimToNull(request.description()));
        plan.setPriceCents(request.priceCents());
        plan.setPeriodDays(request.periodDays());
        plan.setPriceSuffix(trimToNull(request.priceSuffix()));
        plan.setBenefitsJson(toJson(request.benefits()));
        plan.setTrialAvailable(request.trialAvailable());
        plan.setContactSales(request.contactSales());
        plan.setEnabled(request.enabled());
        plan.setVisibleInCatalog(request.visibleInCatalog());
        plan.setSortOrder(request.sortOrder());

        return toAdminResponse(repository.save(plan));
    }

    public SubscriptionPlanCatalog requireEnabledPlan(SubscriptionPlan planCode) {
        SubscriptionPlanCatalog plan = repository.findByPlanCode(planCode)
                .orElseThrow(() -> new BusinessException("Plano não configurado: " + planCode));
        if (!plan.isEnabled()) {
            throw new BusinessException("Este plano não está disponível no momento.");
        }
        return plan;
    }

    public int priceCents(SubscriptionPlan planCode) {
        return repository.findByPlanCode(planCode)
                .filter(SubscriptionPlanCatalog::isEnabled)
                .map(SubscriptionPlanCatalog::getPriceCents)
                .orElseGet(() -> fallbackPrice(planCode));
    }

    public int periodDays(SubscriptionPlan planCode) {
        return repository.findByPlanCode(planCode)
                .filter(SubscriptionPlanCatalog::isEnabled)
                .map(SubscriptionPlanCatalog::getPeriodDays)
                .orElseGet(() -> fallbackPeriodDays(planCode));
    }

    public String planName(SubscriptionPlan planCode) {
        return repository.findByPlanCode(planCode)
                .map(SubscriptionPlanCatalog::getName)
                .orElseGet(() -> fallbackName(planCode));
    }

    public int monthlyPeriodDays() {
        return periodDays(SubscriptionPlan.ATHLETE_MONTHLY);
    }

    private PlanCatalogItemResponse toCatalogItem(SubscriptionPlanCatalog plan) {
        PlanCatalogItemResponse item = new PlanCatalogItemResponse();
        item.setPlan(plan.getPlanCode());
        item.setNome(plan.getName());
        item.setDescricao(plan.getDescription());
        item.setPriceCents(plan.getPriceCents());
        item.setPriceLabel(formatPriceLabel(plan));
        item.setBeneficios(parseBenefits(plan.getBenefitsJson()));
        item.setContatoComercial(plan.isContactSales());
        item.setTrialDisponivel(plan.isTrialAvailable());
        return item;
    }

    private AdminSubscriptionPlanResponse toAdminResponse(SubscriptionPlanCatalog plan) {
        return new AdminSubscriptionPlanResponse(
                plan.getId(),
                plan.getPlanCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getPriceCents(),
                plan.getPeriodDays(),
                plan.getPriceSuffix(),
                parseBenefits(plan.getBenefitsJson()),
                plan.isTrialAvailable(),
                plan.isContactSales(),
                plan.isEnabled(),
                plan.isVisibleInCatalog(),
                plan.getSortOrder(),
                plan.getUpdatedAt()
        );
    }

    private String formatPriceLabel(SubscriptionPlanCatalog plan) {
        if (plan.getPriceCents() == 0) {
            return "Grátis";
        }
        double valor = plan.getPriceCents() / 100.0;
        String formatted = String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
        if (plan.getPriceSuffix() != null && !plan.getPriceSuffix().isBlank()) {
            return formatted + plan.getPriceSuffix();
        }
        return formatted;
    }

    private List<String> parseBenefits(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(List<String> benefits) {
        try {
            return objectMapper.writeValueAsString(benefits != null ? benefits : List.of());
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static int fallbackPrice(SubscriptionPlan plan) {
        return switch (plan) {
            case ATHLETE_MONTHLY -> 2490;
            case ATHLETE_YEARLY -> 19900;
            default -> 0;
        };
    }

    private static int fallbackPeriodDays(SubscriptionPlan plan) {
        return switch (plan) {
            case ATHLETE_YEARLY -> 365;
            case ATHLETE_MONTHLY -> 30;
            default -> 0;
        };
    }

    private static String fallbackName(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> "Grátis";
            case ATHLETE_MONTHLY -> "Atleta Mensal";
            case ATHLETE_YEARLY -> "Atleta Anual";
        };
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }
}
