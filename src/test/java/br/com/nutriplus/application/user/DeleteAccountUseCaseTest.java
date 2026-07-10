package br.com.nutriplus.application.user;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.domain.entity.CareRelationship;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.DeleteAccountRequest;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.InvalidCredentialsException;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.AuditLogService;
import br.com.nutriplus.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private NutritionistRepository nutritionistRepository;
    @Mock private CareRelationshipRepository careRelationshipRepository;
    @Mock private PasswordHasherPort passwordHasherPort;
    @Mock private SubscriptionService subscriptionService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private DeleteAccountUseCase deleteAccountUseCase;

    private User patient;

    @BeforeEach
    void setUp() {
        patient = User.builder()
                .id(10L)
                .email("paciente@example.com")
                .name("Paciente")
                .passwordHash("hash")
                .role(UserRole.PATIENT)
                .build();
    }

    @Test
    void execute_deletes_when_password_and_email_match() {
        when(passwordHasherPort.matches("senha123", "hash")).thenReturn(true);

        deleteAccountUseCase.execute(patient, new DeleteAccountRequest("senha123", "paciente@example.com"));

        verify(subscriptionService).cancelar(10L);
        verify(auditLogService).log("ACCOUNT_DELETE_REQUEST", "USER", patient);
        verify(userRepository).delete(patient);
    }

    @Test
    void execute_rejects_wrong_password() {
        when(passwordHasherPort.matches("errada", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                deleteAccountUseCase.execute(patient, new DeleteAccountRequest("errada", "paciente@example.com")));

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void execute_rejects_admin_accounts() {
        User admin = User.builder()
                .id(10L)
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .build();

        assertThrows(BusinessException.class, () ->
                deleteAccountUseCase.execute(admin, new DeleteAccountRequest("senha123", "admin@example.com")));

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void execute_rejects_nutritionist_with_active_patients() {
        User nutritionistUser = User.builder()
                .id(10L)
                .email("nutri@example.com")
                .passwordHash("hash")
                .role(UserRole.NUTRITIONIST)
                .build();
        Nutritionist nutritionist = mock(Nutritionist.class);
        when(nutritionist.getId()).thenReturn(5L);
        when(nutritionistRepository.findByUserId(10L)).thenReturn(Optional.of(nutritionist));
        when(careRelationshipRepository.findByNutritionistIdAndStatusInOrderByUpdatedAtDesc(
                5L, List.of(CareRelationshipStatus.ACTIVE))).thenReturn(List.of(mock(CareRelationship.class)));

        assertThrows(BusinessException.class, () ->
                deleteAccountUseCase.execute(
                        nutritionistUser,
                        new DeleteAccountRequest("senha123", "nutri@example.com")));

        verify(userRepository, never()).delete(any(User.class));
    }
}
