package br.com.nutriplus.service;

import br.com.nutriplus.infrastructure.config.NutriCacheNames;
import br.com.nutriplus.domain.entity.AppFeatureFlag;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.UpdateFeatureFlagRequest;
import br.com.nutriplus.dto.response.FeatureFlagResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.AppFeatureFlagRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeatureFlagService {

    private final AppFeatureFlagRepository featureFlagRepository;
    private final AuthorizationService authorizationService;

    public FeatureFlagService(AppFeatureFlagRepository featureFlagRepository,
                              AuthorizationService authorizationService) {
        this.featureFlagRepository = featureFlagRepository;
        this.authorizationService = authorizationService;
    }

    @Cacheable(value = NutriCacheNames.FEATURE_FLAGS, key = "'public'")
    public List<FeatureFlagResponse> listPublic() {
        return featureFlagRepository.findAllByOrderByCategoryAscCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<FeatureFlagResponse> listForAdmin() {
        requireAdmin();
        return listPublic();
    }

    public boolean isEnabled(String code) {
        return featureFlagRepository.findByCode(code)
                .map(AppFeatureFlag::isEnabled)
                .orElse(false);
    }

    @Transactional
    @CacheEvict(value = NutriCacheNames.FEATURE_FLAGS, allEntries = true)
    public FeatureFlagResponse update(String code, UpdateFeatureFlagRequest request) {
        requireAdmin();
        AppFeatureFlag flag = featureFlagRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag não encontrada."));
        flag.setEnabled(request.enabled());
        flag.setUpdatedBy(authorizationService.currentUserId());
        featureFlagRepository.save(flag);
        return toResponse(flag);
    }

    private FeatureFlagResponse toResponse(AppFeatureFlag flag) {
        return new FeatureFlagResponse(
                flag.getCode(),
                flag.getName(),
                flag.getDescription(),
                flag.getCategory(),
                flag.isEnabled(),
                flag.getUpdatedAt()
        );
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }
}
