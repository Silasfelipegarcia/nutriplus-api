package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.dto.response.NutritionistPendingResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.infrastructure.security.CpfProtectionService;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminNutritionistService {

    private final AuthorizationService authorizationService;
    private final NutritionistRepository nutritionistRepository;
    private final CpfProtectionService cpfProtectionService;

    public AdminNutritionistService(AuthorizationService authorizationService,
                                    NutritionistRepository nutritionistRepository,
                                    CpfProtectionService cpfProtectionService) {
        this.authorizationService = authorizationService;
        this.nutritionistRepository = nutritionistRepository;
        this.cpfProtectionService = cpfProtectionService;
    }

    public List<NutritionistPendingResponse> listPendingVerification() {
        requireAdmin();
        return nutritionistRepository.findByCrnVerifiedFalseOrderByCreatedAtAsc().stream()
                .map(this::toPending)
                .toList();
    }

    @Transactional
    public void verify(Long nutritionistId) {
        requireAdmin();
        Nutritionist n = nutritionistRepository.findById(nutritionistId)
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista não encontrado."));
        n.setCrnVerified(true);
        nutritionistRepository.save(n);
    }

    @Transactional
    public void reject(Long nutritionistId) {
        requireAdmin();
        Nutritionist n = nutritionistRepository.findById(nutritionistId)
                .orElseThrow(() -> new ResourceNotFoundException("Nutricionista não encontrado."));
        n.setCrnVerified(false);
        n.setMarketplaceVisible(false);
        nutritionistRepository.save(n);
    }

    private NutritionistPendingResponse toPending(Nutritionist n) {
        var user = n.getUser();
        return new NutritionistPendingResponse(
                n.getId(),
                user.getName(),
                user.getEmail(),
                n.getCrn(),
                cpfProtectionService.maskFromEncrypted(user.getCpfEncrypted()),
                n.isMarketplaceVisible()
        );
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(br.com.nutriplus.domain.enums.UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }
}
