package br.com.nutriplus.security;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.PatientDataConsentRepository;
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
class AuthorizationServiceRequireNutritionistTest {

    @Mock private CurrentUser currentUser;
    @Mock private NutritionistRepository nutritionistRepository;
    @Mock private CareRelationshipRepository careRelationshipRepository;
    @Mock private PatientDataConsentRepository consentRepository;

    private AuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new AuthorizationService(
                currentUser, nutritionistRepository, careRelationshipRepository, consentRepository);
    }

    @Test
    void requireNutritionistUsesFindByUserIdWhichFetchesUser() {
        User user = User.builder()
                .id(18L)
                .name("Ana Nutri")
                .email("ana@nutriplus.test")
                .role(UserRole.NUTRITIONIST)
                .build();
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-SP 12345", "bio", "esp", 7900, 30);
        when(currentUser.get()).thenReturn(user);
        when(nutritionistRepository.findByUserId(18L)).thenReturn(Optional.of(nutritionist));

        Nutritionist loaded = service.requireNutritionist();

        assertThat(loaded).isSameAs(nutritionist);
        assertThat(loaded.getUser().getName()).isEqualTo("Ana Nutri");
        verify(nutritionistRepository).findByUserId(18L);
    }
}
