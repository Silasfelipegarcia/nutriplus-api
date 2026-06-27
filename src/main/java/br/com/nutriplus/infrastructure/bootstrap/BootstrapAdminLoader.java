package br.com.nutriplus.infrastructure.bootstrap;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class BootstrapAdminLoader {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminLoader.class);

    @Bean
    CommandLineRunner bootstrapAdmin(UserRepository userRepository,
                                     @Value("${nutriplus.admin.bootstrap-email:}") String bootstrapEmail) {
        return args -> {
            if (bootstrapEmail == null || bootstrapEmail.isBlank()) {
                return;
            }
            if (userRepository.countByRole(UserRole.ADMIN) > 0) {
                return;
            }
            userRepository.findByEmail(bootstrapEmail.trim().toLowerCase()).ifPresentOrElse(user -> {
                user.setRole(UserRole.ADMIN);
                user.setLoginEnabled(true);
                user.setLoginEnabledAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Bootstrap admin promoted: {}", bootstrapEmail);
            }, () -> log.warn("Bootstrap admin email not found (no user yet): {}", bootstrapEmail));
        };
    }
}
