package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.CareRating;
import br.com.nutriplus.domain.entity.CareRelationship;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.dto.request.CareRatingRequest;
import br.com.nutriplus.dto.response.CareRatingResponse;
import br.com.nutriplus.dto.response.NutritionistRatingsSummaryResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.CareRatingRepository;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CareRatingService {

    private final CurrentUser currentUser;
    private final CareRelationshipRepository careRelationshipRepository;
    private final CareRatingRepository careRatingRepository;

    public CareRatingService(CurrentUser currentUser,
                             CareRelationshipRepository careRelationshipRepository,
                             CareRatingRepository careRatingRepository) {
        this.currentUser = currentUser;
        this.careRelationshipRepository = careRelationshipRepository;
        this.careRatingRepository = careRatingRepository;
    }

    @Transactional
    public CareRatingResponse rate(Long careRelationshipId, CareRatingRequest request) {
        User patient = currentUser.get();
        CareRelationship care = careRelationshipRepository.findById(careRelationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado."));
        if (!care.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException("Sem permissão para avaliar este vínculo.");
        }
        if (care.getStatus() != CareRelationshipStatus.ACTIVE
                && care.getStatus() != CareRelationshipStatus.EXPIRED) {
            throw new BusinessException("Avaliação disponível após acompanhamento ativo.");
        }
        if (careRatingRepository.findByCareRelationshipId(careRelationshipId).isPresent()) {
            throw new BusinessException("Este vínculo já foi avaliado.");
        }

        CareRating rating = careRatingRepository.save(
                CareRating.create(care, request.stars(), request.comment()));
        return toResponse(rating);
    }

    public NutritionistRatingsSummaryResponse summaryForNutritionist(Long nutritionistId) {
        Double avg = careRatingRepository.averageStarsByNutritionistId(nutritionistId);
        long count = careRatingRepository.countByNutritionistId(nutritionistId);
        List<CareRatingResponse> recent = careRatingRepository
                .findTop10ByNutritionistIdOrderByCreatedAtDesc(nutritionistId).stream()
                .map(this::toResponse)
                .toList();
        return new NutritionistRatingsSummaryResponse(avg != null ? avg : 0.0, count, recent);
    }

    public NutritionistRatingsSummaryResponse myRatings(Long nutritionistId) {
        return summaryForNutritionist(nutritionistId);
    }

    private CareRatingResponse toResponse(CareRating rating) {
        return new CareRatingResponse(
                rating.getId(),
                rating.getCareRelationship().getId(),
                rating.getStars(),
                rating.getComment(),
                rating.getPatient().getName(),
                rating.getCreatedAt()
        );
    }
}
