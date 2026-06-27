package br.com.nutriplus.infrastructure.dev;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.*;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@Profile({"local", "dev"})
public class DevDataLoader {

    private static final Logger log = LoggerFactory.getLogger(DevDataLoader.class);

    public static final String TEST_EMAIL = "teste@nutriplus.local";
    public static final String TEST_PASSWORD = "Nutri123!";

    public static final String TEST2_EMAIL = "teste2@nutriplus.local";
    public static final String ADMIN_EMAIL = "admin@nutriplus.local";
    public static final String ADMIN_PASSWORD = "Nutri123!";

    @Bean
    CommandLineRunner seedTestUser(
            UserRepository userRepository,
            NutritionProfileRepository nutritionProfileRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            seedAdmin(userRepository, passwordEncoder);
            seedPatient(userRepository, nutritionProfileRepository, passwordEncoder,
                    TEST_EMAIL, "Usuário Teste", true);
            seedPatient(userRepository, nutritionProfileRepository, passwordEncoder,
                    TEST2_EMAIL, "Usuário Teste 2", false);
        };
    }

    private void seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }
        User admin = User.builder()
                .name("Admin Nutri+")
                .email(ADMIN_EMAIL)
                .role(UserRole.ADMIN)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .loginEnabled(true)
                .loginEnabledAt(LocalDateTime.now())
                .build();
        userRepository.save(admin);
        log.info("Dev admin user created: {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private void seedPatient(UserRepository userRepository,
                               NutritionProfileRepository nutritionProfileRepository,
                               PasswordEncoder passwordEncoder,
                               String email,
                               String name,
                               boolean withProfile) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .loginEnabled(true)
                .loginEnabledAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        if (withProfile) {
            NutritionProfile profile = NutritionProfile.builder()
                    .user(user)
                    .age(30)
                    .sex(Sex.MALE)
                    .heightCm(new BigDecimal("175.00"))
                    .currentWeightKg(new BigDecimal("80.00"))
                    .targetWeightKg(new BigDecimal("75.00"))
                    .goal(Goal.LOSE_WEIGHT)
                    .activityLevel(ActivityLevel.MODERATE)
                    .dietaryPreference(DietaryPreference.OMNIVORE)
                    .restriction(Restriction.NONE)
                    .agentPersona(AgentPersona.LUNA)
                    .foodLikes("frango, arroz, salada, banana")
                    .foodDislikes("fígado, jiló")
                    .mealNotes("Prefiro café reforçado e almoço prático")
                    .bmrKcal(new BigDecimal("1750.00"))
                    .tdeeKcal(new BigDecimal("2400.00"))
                    .targetCalories(new BigDecimal("1900.00"))
                    .targetProteinG(new BigDecimal("150.00"))
                    .targetCarbsG(new BigDecimal("190.00"))
                    .targetFatG(new BigDecimal("63.00"))
                    .build();
            nutritionProfileRepository.save(profile);
        }

        log.info("Dev test user created: {} / {} (login liberado)", email, TEST_PASSWORD);
    }
}
