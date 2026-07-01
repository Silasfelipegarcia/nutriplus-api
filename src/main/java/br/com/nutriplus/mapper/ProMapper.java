package br.com.nutriplus.mapper;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.ServiceMode;
import br.com.nutriplus.domain.util.LanguageCodec;
import br.com.nutriplus.domain.util.ServiceModeCodec;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.repository.CareRatingRepository;
import br.com.nutriplus.service.NutritionistPortfolioService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProMapper {

    private final CareRatingRepository careRatingRepository;
    private final NutritionistPortfolioService portfolioService;

    public ProMapper(CareRatingRepository careRatingRepository,
                     NutritionistPortfolioService portfolioService) {
        this.careRatingRepository = careRatingRepository;
        this.portfolioService = portfolioService;
    }

    public NutritionistPublicResponse toPublic(Nutritionist n) {
        return toNutritionistResponse(n, false, loadRatings(List.of(n.getId())).getOrDefault(n.getId(), NutritionistRatingSummary.empty()),
                portfolioService.listForNutritionist(n.getId()));
    }

    public NutritionistPublicResponse toPublic(Nutritionist n, NutritionistRatingSummary rating) {
        return toNutritionistResponse(n, false, rating, portfolioService.listForNutritionist(n.getId()));
    }

    public List<NutritionistPublicResponse> toPublicList(List<Nutritionist> nutritionists) {
        if (nutritionists.isEmpty()) {
            return List.of();
        }
        List<Long> ids = nutritionists.stream().map(Nutritionist::getId).toList();
        Map<Long, NutritionistRatingSummary> ratings = loadRatings(ids);
        Map<Long, List<NutritionistPortfolioItemResponse>> portfolios = portfolioService.listByNutritionistIds(ids);
        return nutritionists.stream()
                .map(n -> toNutritionistResponse(n, false,
                        ratings.getOrDefault(n.getId(), NutritionistRatingSummary.empty()),
                        portfolios.getOrDefault(n.getId(), List.of())))
                .toList();
    }

    public Map<Long, NutritionistRatingSummary> loadRatings(Collection<Long> nutritionistIds) {
        if (nutritionistIds == null || nutritionistIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, NutritionistRatingSummary> ratings = new HashMap<>();
        for (Object[] row : careRatingRepository.avgStarsAndCountByNutritionistIds(nutritionistIds)) {
            Long id = ((Number) row[0]).longValue();
            ratings.put(id, NutritionistRatingSummary.fromRow(row));
        }
        return ratings;
    }

    public NutritionistPublicResponse toProfile(Nutritionist n) {
        return toNutritionistResponse(n, true, loadRatings(List.of(n.getId())).getOrDefault(n.getId(), NutritionistRatingSummary.empty()),
                portfolioService.listForNutritionist(n.getId()));
    }

    public NutritionistPublicResponse toProfile(Nutritionist n, List<NutritionistPortfolioItemResponse> portfolio) {
        return toNutritionistResponse(n, true, loadRatings(List.of(n.getId())).getOrDefault(n.getId(), NutritionistRatingSummary.empty()),
                portfolio);
    }

    private NutritionistPublicResponse toNutritionistResponse(Nutritionist n, boolean includePrivateContact,
                                                              NutritionistRatingSummary rating,
                                                              List<NutritionistPortfolioItemResponse> portfolioItems) {
        User u = n.getUser();
        Set<ServiceMode> modes = ServiceModeCodec.decode(n.getServiceModes());
        List<String> modeNames = modes.stream().map(Enum::name).sorted().toList();
        return new NutritionistPublicResponse(
                n.getId(),
                u.getName(),
                n.getCrn(),
                n.isCrnVerified(),
                n.getBio(),
                n.getSpecialties(),
                n.getConsultationPriceCents(),
                n.getCareDurationDays(),
                u.getPhotoThumbnailUrl(),
                modeNames,
                n.getCity(),
                n.getStateCode(),
                buildLocationLabel(n, modes),
                includePrivateContact ? n.getWhatsappPhone() : null,
                rating.averageStars(),
                rating.count(),
                n.getFormation(),
                n.getExperienceYears(),
                n.getApproach(),
                LanguageCodec.decode(n.getLanguages()),
                portfolioItems
        );
    }

    static String buildLocationLabel(Nutritionist n, Set<ServiceMode> modes) {
        List<String> parts = new ArrayList<>();
        if (modes.contains(ServiceMode.IN_PERSON) && n.getCity() != null && n.getStateCode() != null) {
            parts.add(n.getCity() + ", " + n.getStateCode());
        }
        if (modes.contains(ServiceMode.ONLINE) && modes.contains(ServiceMode.IN_PERSON)) {
            parts.add("Online e presencial");
        } else if (modes.contains(ServiceMode.ONLINE)) {
            parts.add("Somente online");
        } else if (modes.contains(ServiceMode.IN_PERSON)) {
            parts.add("Somente presencial");
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    public PricingGuidelinesResponse toPricing(PricingGuideline g) {
        return new PricingGuidelinesResponse(
                g.getMinConsultationPriceCents(),
                g.getMaxConsultationPriceCents(),
                g.getSuggestedPriceCents(),
                g.getPlatformFeePercent(),
                g.getCareDurationDaysDefault()
        );
    }

    public CareRelationshipResponse toCare(CareRelationship cr) {
        return new CareRelationshipResponse(
                cr.getId(),
                cr.getPatient().getId(),
                cr.getPatient().getName(),
                cr.getNutritionist().getId(),
                cr.getNutritionist().getUser().getName(),
                cr.getStatus(),
                cr.getSource(),
                cr.getPreferredCareMode(),
                cr.getStartedAt(),
                cr.getExpiresAt(),
                cr.getCreatedAt()
        );
    }

    public InviteResponse toInvite(NutritionistInvite invite, String baseUrl) {
        String url = baseUrl + "/convite/" + invite.getCode();
        return new InviteResponse(
                invite.getCode(),
                url,
                invite.getMaxUses(),
                invite.getUseCount(),
                invite.getExpiresAt(),
                invite.getCreatedAt()
        );
    }

    public MessageResponse toMessage(Message m, Long currentUserId) {
        return new MessageResponse(
                m.getId(),
                m.getSender().getId(),
                m.getSender().getName(),
                m.getBody(),
                m.getReadAt(),
                m.getCreatedAt(),
                m.getSender().getId().equals(currentUserId)
        );
    }
}
