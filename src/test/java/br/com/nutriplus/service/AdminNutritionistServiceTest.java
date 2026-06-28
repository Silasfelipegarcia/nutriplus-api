package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.infrastructure.security.CpfProtectionService;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNutritionistServiceTest {

    @Mock private AuthorizationService authorizationService;
    @Mock private NutritionistRepository nutritionistRepository;
    @Mock private UserRepository userRepository;
    @Mock private CpfProtectionService cpfProtectionService;
    @Mock private BetaAccessNotificationService betaAccessNotificationService;

    private AdminNutritionistService service;

    @BeforeEach
    void setUp() {
        service = new AdminNutritionistService(
                authorizationService,
                nutritionistRepository,
                userRepository,
                cpfProtectionService,
                betaAccessNotificationService);
    }

    @Test
    void verifyEnablesLoginAndCrn() {
        User user = User.builder()
                .id(10L)
                .name("Nutri")
                .email("nutri@test.com")
                .role(UserRole.NUTRITIONIST)
                .loginEnabled(false)
                .build();
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-1", "bio", "esp", 7900, 30);

        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(nutritionistRepository.findById(7L)).thenReturn(Optional.of(nutritionist));
        when(authorizationService.currentUserId()).thenReturn(1L);

        service.verify(7L);

        assertThat(nutritionist.isCrnVerified()).isTrue();
        assertThat(user.isLoginEnabled()).isTrue();
        verify(userRepository).save(user);
        verify(nutritionistRepository).save(nutritionist);
        verify(betaAccessNotificationService).notifyApproved(user);
    }

    @Test
    void rejectHidesFromMarketplace() {
        User user = User.builder().id(11L).name("N").email("n2@test.com").role(UserRole.NUTRITIONIST).build();
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-2", null, null, 7900, 30);
        nutritionist.setMarketplaceVisible(true);

        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(nutritionistRepository.findById(8L)).thenReturn(Optional.of(nutritionist));

        service.reject(8L);

        assertThat(nutritionist.isCrnVerified()).isFalse();
        assertThat(nutritionist.isMarketplaceVisible()).isFalse();
    }
}
