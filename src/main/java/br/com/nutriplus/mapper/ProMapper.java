package br.com.nutriplus.mapper;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.ServiceMode;
import br.com.nutriplus.domain.util.ServiceModeCodec;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.repository.CareRatingRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ProMapper {

    private final CareRatingRepository careRatingRepository;

    public ProMapper(CareRatingRepository careRatingRepository) {
        this.careRatingRepository = careRatingRepository;
    }

    public NutritionistPublicResponse toPublic(Nutritionist n) {
        return toNutritionistResponse(n, false);
    }

    public NutritionistPublicResponse toProfile(Nutritionist n) {
        return toNutritionistResponse(n, true);
    }

    private NutritionistPublicResponse toNutritionistResponse(Nutritionist n, boolean includePrivateContact) {
        User u = n.getUser();
        Set<ServiceMode> modes = ServiceModeCodec.decode(n.getServiceModes());
        List<String> modeNames = modes.stream().map(Enum::name).sorted().toList();
        Double avg = careRatingRepository.averageStarsByNutritionistId(n.getId());
        long count = careRatingRepository.countByNutritionistId(n.getId());
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
                avg != null ? avg : 0.0,
                count
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
        String url = baseUrl + "/invite/" + invite.getCode();
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
