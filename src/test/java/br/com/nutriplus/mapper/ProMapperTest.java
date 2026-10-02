package br.com.nutriplus.mapper;

import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.repository.CareRatingRepository;
import br.com.nutriplus.service.NutritionistPortfolioService;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProMapperTest {

    @Mock
    private CareRatingRepository careRatingRepository;

    @Mock
    private NutritionistPortfolioService portfolioService;

    private ProMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProMapper(careRatingRepository, portfolioService);
    }

    @Test
    void toProfileReadsNameAndPhotoFromInitializedUser() {
        User user = User.builder()
                .id(18L)
                .name("Ana Nutri")
                .email("ana@nutriplus.test")
                .role(UserRole.NUTRITIONIST)
                .photoThumbnailUrl("thumb")
                .build();
        Nutritionist nutritionist = nutritionistWithId(7L, user);
        when(careRatingRepository.avgStarsAndCountByNutritionistIds(any())).thenReturn(List.of());
        when(portfolioService.listForNutritionist(7L)).thenReturn(List.of());

        var response = mapper.toProfile(nutritionist);

        assertThat(response.name()).isEqualTo("Ana Nutri");
        assertThat(response.photoThumbnailUrl()).isEqualTo("thumb");
        assertThat(response.crn()).isEqualTo("CRN-SP 12345");
    }

    @Test
    void toProfileWouldFailWhenUserProxyIsNotInitialized() {
        User lazyUser = mock(User.class);
        when(lazyUser.getName()).thenThrow(new LazyInitializationException(
                "could not initialize proxy [br.com.nutriplus.domain.entity.User#18] - no Session"));
        Nutritionist nutritionist = nutritionistWithId(7L, lazyUser);
        when(careRatingRepository.avgStarsAndCountByNutritionistIds(any())).thenReturn(List.of());
        when(portfolioService.listForNutritionist(7L)).thenReturn(List.of());

        assertThatThrownBy(() -> mapper.toProfile(nutritionist))
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("no Session");
    }

    private static Nutritionist nutritionistWithId(Long id, User user) {
        Nutritionist nutritionist = Nutritionist.createFor(user, "CRN-SP 12345", "bio", "esp", 7900, 30);
        try {
            var field = Nutritionist.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(nutritionist, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return nutritionist;
    }
}
