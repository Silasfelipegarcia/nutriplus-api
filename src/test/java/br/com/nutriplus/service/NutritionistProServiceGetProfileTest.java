package br.com.nutriplus.service;

import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.application.port.TokenPort;
import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.CareRatingRepository;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionistProServiceGetProfileTest {

    @Mock private AuthorizationService authorizationService;
    @Mock private CareRatingRepository careRatingRepository;
    @Mock private NutritionistPortfolioService portfolioService;

    private NutritionistProService service;

    @BeforeEach
    void setUp() {
        ProMapper proMapper = new ProMapper(careRatingRepository, portfolioService);
        service = new NutritionistProService(
                mock(UserRepository.class),
                mock(NutritionistRepository.class),
                mock(NutritionProfileRepository.class),
                mock(PasswordHasherPort.class),
                mock(TokenPort.class),
                mock(UserQueryPort.class),
                mock(ResponseMapper.class),
                proMapper,
                mock(PricingGuidelineService.class),
                authorizationService,
                mock(AuditLogService.class),
                mock(CpfRegistrationService.class),
                mock(UserRegistrationValidator.class),
                mock(FeatureFlagService.class));
    }

    @Test
    void getMyProfileSucceedsWithInitializedUser() {
        Nutritionist nutritionist = nutritionist(18L, "Ana Nutri");
        when(authorizationService.requireNutritionist()).thenReturn(nutritionist);
        when(careRatingRepository.avgStarsAndCountByNutritionistIds(any())).thenReturn(List.of());
        when(portfolioService.listForNutritionist(7L)).thenReturn(List.of());

        var response = service.getMyProfile();

        assertThat(response.name()).isEqualTo("Ana Nutri");
        assertThat(response.crn()).isEqualTo("CRN-SP 12345");
    }

    @Test
    void getMyProfileDoesNotFailWhenCpfCannotBeDecrypted() {
        Nutritionist nutritionist = nutritionist(18L, "Ana Nutri");
        nutritionist.getUser().setCpfEncrypted("not-valid-ciphertext");
        when(authorizationService.requireNutritionist()).thenReturn(nutritionist);
        when(careRatingRepository.avgStarsAndCountByNutritionistIds(any())).thenReturn(List.of());
        when(portfolioService.listForNutritionist(7L)).thenReturn(List.of());

        assertThatCode(() -> service.getMyProfile()).doesNotThrowAnyException();
        assertThat(service.getMyProfile().name()).isEqualTo("Ana Nutri");
    }

    private static Nutritionist nutritionist(Long userId, String name) {
        User user = User.builder()
                .id(userId)
                .name(name)
                .email("ana@nutriplus.test")
                .role(UserRole.NUTRITIONIST)
                .build();
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-SP 12345", "bio", "esp", 7900, 30);
        try {
            Field id = Nutritionist.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(nutritionist, 7L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return nutritionist;
    }
}
