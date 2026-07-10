package br.com.nutriplus.application.user;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.service.AuditLogService;
import br.com.nutriplus.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminDeleteUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private NutritionistRepository nutritionistRepository;
    @Mock private CareRelationshipRepository careRelationshipRepository;
    @Mock private SubscriptionService subscriptionService;
    @Mock private AuditLogService auditLogService;

    private AdminDeleteUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AdminDeleteUserUseCase(
                userRepository,
                nutritionistRepository,
                careRelationshipRepository,
                subscriptionService,
                auditLogService);
    }

    @Test
    void executeDeletesPatient() {
        User user = User.builder().id(2L).name("P").email("p@test.com").role(UserRole.PATIENT).build();

        useCase.execute(user, 1L);

        verify(userRepository).delete(user);
        verify(auditLogService).log("ADMIN_USER_DELETE", "USER", user);
    }

    @Test
    void executeBlocksSelfDelete() {
        User user = User.builder().id(1L).name("A").email("a@test.com").role(UserRole.PATIENT).build();

        assertThatThrownBy(() -> useCase.execute(user, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("própria conta");

        verifyNoInteractions(userRepository);
    }

    @Test
    void executeBlocksAdminDelete() {
        User user = User.builder().id(2L).name("A").email("a@test.com").role(UserRole.ADMIN).build();

        assertThatThrownBy(() -> useCase.execute(user, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("administrador");

        verifyNoInteractions(userRepository);
    }
}
