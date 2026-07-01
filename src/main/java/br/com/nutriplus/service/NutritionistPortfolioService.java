package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.NutritionistPortfolioItem;
import br.com.nutriplus.dto.request.ProPortfolioUpdateRequest;
import br.com.nutriplus.dto.response.NutritionistPortfolioItemResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.NutritionistPortfolioItemRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionistPortfolioService {

    private static final int MAX_ITEMS = 5;

    private final AuthorizationService authorizationService;
    private final NutritionistPortfolioItemRepository portfolioRepository;

    public NutritionistPortfolioService(AuthorizationService authorizationService,
                                        NutritionistPortfolioItemRepository portfolioRepository) {
        this.authorizationService = authorizationService;
        this.portfolioRepository = portfolioRepository;
    }

    public List<NutritionistPortfolioItemResponse> listForNutritionist(Long nutritionistId) {
        return portfolioRepository.findByNutritionistIdOrderBySortOrderAsc(nutritionistId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<Long, List<NutritionistPortfolioItemResponse>> listByNutritionistIds(Collection<Long> nutritionistIds) {
        if (nutritionistIds == null || nutritionistIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<NutritionistPortfolioItemResponse>> result = new HashMap<>();
        for (NutritionistPortfolioItem item : portfolioRepository
                .findByNutritionistIdInOrderByNutritionistIdAscSortOrderAsc(nutritionistIds)) {
            result.computeIfAbsent(item.getNutritionist().getId(), id -> new ArrayList<>())
                    .add(toResponse(item));
        }
        return result;
    }

    @Transactional
    public List<NutritionistPortfolioItemResponse> replacePortfolio(ProPortfolioUpdateRequest request) {
        Nutritionist nutritionist = authorizationService.requireNutritionist();
        List<ProPortfolioUpdateRequest.PortfolioItemInput> items =
                request.items() != null ? request.items() : List.of();
        if (items.size() > MAX_ITEMS) {
            throw new BusinessException("Máximo de " + MAX_ITEMS + " casos no portfólio.");
        }

        portfolioRepository.deleteByNutritionistId(nutritionist.getId());
        int order = 0;
        for (ProPortfolioUpdateRequest.PortfolioItemInput input : items) {
            portfolioRepository.save(NutritionistPortfolioItem.create(
                    nutritionist,
                    input.title().trim(),
                    input.summary().trim(),
                    order++));
        }
        return listForNutritionist(nutritionist.getId());
    }

    private NutritionistPortfolioItemResponse toResponse(NutritionistPortfolioItem item) {
        return new NutritionistPortfolioItemResponse(
                item.getId(),
                item.getTitle(),
                item.getSummary(),
                item.getSortOrder());
    }
}
