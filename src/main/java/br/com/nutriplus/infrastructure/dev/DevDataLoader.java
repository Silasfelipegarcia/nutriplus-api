package br.com.nutriplus.infrastructure.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"})
public class DevDataLoader {

    public static final String TEST_EMAIL = "teste@nutriplus.local";
    public static final String TEST_PASSWORD = "Nutri123!";

    public static final String TEST2_EMAIL = "teste2@nutriplus.local";
    public static final String ADMIN_EMAIL = "admin@nutriplus.local";
    public static final String ADMIN_PASSWORD = "Nutri123!";

    @Bean
    CommandLineRunner seedTestUser(DevFunctionalTestSeeder functionalTestSeeder) {
        return args -> functionalTestSeeder.seedAll();
    }
}
