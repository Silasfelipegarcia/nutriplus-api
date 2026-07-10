package br.com.nutriplus.service;

import br.com.nutriplus.application.user.AdminDeleteUserUseCase;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.RejectUserAccessRequest;
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

import java.time.LocalDateTime;
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
    @Mock private BetaAccessNotificationService betaAccessNotificationService;
    @Mock private AdminDeleteUserUseCase adminDeleteUserUseCase;

    private AdminAccessService service;

    @BeforeEach
    void setUp() {
        service = new AdminAccessService(
                authorizationService,
                userRepository,
                nutritionProfileRepository,
                nutritionistRepository,
                betaAccessNotificationService,
                adminDeleteUserUseCase);
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

    @Test
    void setLoginEnabledSendsBetaApprovalEmailOnFirstEnable() {
        User user = patient(3L);
        user.setLoginEnabled(false);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(authorizationService.currentUserId()).thenReturn(1L);
        when(nutritionProfileRepository.findByUserId(3L)).thenReturn(Optional.empty());

        service.setLoginEnabled(3L, new UpdateLoginEnabledRequest(true));

        verify(betaAccessNotificationService).notifyApproved(user);
    }

    @Test
    void setLoginEnabledDoesNotSendEmailWhenAlreadyEnabled() {
        User user = patient(4L);
        user.setLoginEnabled(true);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(authorizationService.currentUserId()).thenReturn(1L);
        when(nutritionProfileRepository.findByUserId(4L)).thenReturn(Optional.empty());

        service.setLoginEnabled(4L, new UpdateLoginEnabledRequest(true));

        verify(betaAccessNotificationService, never()).notifyApproved(any());
    }

    @Test
    void rejectAccessMarksUserAndSendsEmail() {
        User user = patient(6L);
        user.setLoginEnabled(false);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(6L)).thenReturn(Optional.of(user));
        when(authorizationService.currentUserId()).thenReturn(1L);
        when(nutritionProfileRepository.findByUserId(6L)).thenReturn(Optional.empty());

        service.rejectAccess(6L, new RejectUserAccessRequest("Perfil fora do beta atual"));

        assertThat(user.isAccessRejected()).isTrue();
        assertThat(user.getAccessRejectionReason()).isEqualTo("Perfil fora do beta atual");
        assertThat(user.isLoginEnabled()).isFalse();
        verify(betaAccessNotificationService).notifyRejected(user, "Perfil fora do beta atual");
        verify(userRepository).save(user);
    }

    @Test
    void rejectAccessFailsWhenAlreadyApproved() {
        User user = patient(7L);
        user.setLoginEnabled(true);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.rejectAccess(7L, new RejectUserAccessRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já possui login liberado");
    }

    @Test
    void rejectAccessFailsWhenAlreadyRejected() {
        User user = patient(8L);
        user.setAccessRejectedAt(LocalDateTime.now());
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.rejectAccess(8L, new RejectUserAccessRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi recusado");
    }

    @Test
    void setLoginEnabledClearsRejectionWhenApproving() {
        User user = patient(9L);
        user.setLoginEnabled(false);
        user.setAccessRejectedAt(LocalDateTime.now());
        user.setAccessRejectionReason("motivo");
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(authorizationService.currentUserId()).thenReturn(1L);
        when(nutritionProfileRepository.findByUserId(9L)).thenReturn(Optional.empty());

        service.setLoginEnabled(9L, new UpdateLoginEnabledRequest(true));

        assertThat(user.isAccessRejected()).isFalse();
        assertThat(user.getAccessRejectionReason()).isNull();
        assertThat(user.isLoginEnabled()).isTrue();
    }

    @Test
    void deleteUserDelegatesToUseCase() {
        User user = patient(10L);
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(adminDeleteUserUseCase.requireUser(10L)).thenReturn(user);
        when(authorizationService.currentUserId()).thenReturn(1L);

        service.deleteUser(10L);

        verify(adminDeleteUserUseCase).execute(user, 1L);
    }

    @Test
    void deleteUserRequiresAdmin() {
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteUser(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("administradores");
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
