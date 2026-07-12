package br.com.nutriplus.infrastructure.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Boot local/dev — delega para {@link FunctionalTestUserSeeder}.
 */
@Component
public class DevFunctionalTestSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevFunctionalTestSeeder.class);

    private final FunctionalTestUserSeeder functionalTestUserSeeder;

    public DevFunctionalTestSeeder(FunctionalTestUserSeeder functionalTestUserSeeder) {
        this.functionalTestUserSeeder = functionalTestUserSeeder;
    }

    public void seedAll() {
        functionalTestUserSeeder.seedAllDevUsers();
        log.info("Dev test users ready — password {} — catalog: docs/DEV_TEST_USERS.md",
                DevDataLoader.TEST_PASSWORD);
    }
}
