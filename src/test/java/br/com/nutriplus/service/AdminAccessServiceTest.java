package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.UpdateLoginEnabledRequest;
import br.com.nutriplus.dto.request.UpdateUserAdminRequest;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccessServiceTest {

    @Mock private AuthorizationService authorizationService;
    @Mock private UserRepository userRepository;
    @Mock private NutritionProfileRepository nutritionProfileRepository;
    @Mock private NutritionistRepository nutritionistRepository;

    private AdminAccessService service;

    @BeforeEach
    void setUp() {
        service = new AdminAccessService(
                authorizationService, userRepository, nutritionProfileRepository, nutritionistRepository);
    }

    @Test
    void setUserAdminPromotesUser() {
        User user = patient(2L);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(authorizationService.currentUserId()).thenReturn(1L);
        when(nutritionProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());

        service.setUserAdmin(2L, new UpdateUserAdminRequest(true));

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.isLoginEnabled()).isTrue();
    }

    @Test
    void setUserAdminCannotRemoveSelf() {
        User admin = admin(1L);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(authorizationService.currentUserId()).thenReturn(1L);

        assertThatThrownBy(() -> service.setUserAdmin(1L, new UpdateUserAdminRequest(false)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("próprio acesso");
    }

    @Test
    void setUserAdminCannotRemoveLastAdmin() {
        User admin = admin(1L);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(authorizationService.currentUserId()).thenReturn(99L);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.setUserAdmin(1L, new UpdateUserAdminRequest(false)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pelo menos um administrador");
    }

    @Test
    void setLoginEnabledForNutritionistAlsoVerifiesCrn() {
        User user = nutritionistUser(5L);
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-1", null, null, 7900, 30);

        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(authorizationService.currentUserId()).thenReturn(1L);
        when(nutritionistRepository.findByUserId(5L)).thenReturn(Optional.of(nutritionist));
        when(nutritionProfileRepository.findByUserId(5L)).thenReturn(Optional.empty());

        service.setLoginEnabled(5L, new UpdateLoginEnabledRequest(true));

        assertThat(nutritionist.isCrnVerified()).isTrue();
        verify(nutritionistRepository).save(nutritionist);
    }

    private static User patient(long id) {
        return User.builder().id(id).name("P").email("p@test.com").role(UserRole.PATIENT).build();
    }

    private static User admin(long id) {
        return User.builder().id(id).name("A").email("a@test.com").role(UserRole.ADMIN).loginEnabled(true).build();
    }

    private static User nutritionistUser(long id) {
        User user = User.builder()
                .id(id)
                .name("N")
                .email("n@test.com")
                .role(UserRole.NUTRITIONIST)
                .registrationSource(RegistrationSource.BETA_WAITLIST)
                .build();
        return user;
    }
}
