package br.com.nutriplus.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Garante que respostas de perfil cacheadas (Redis) não sobrevivam a deploys
 * que curam plan_synced_at — senão o app mobile continua vendo pending falso.
 */
@Component
public class NutritionProfileCacheStartupClear implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NutritionProfileCacheStartupClear.class);

    private final NutriCacheEvictionService cacheEvictionService;

    public NutritionProfileCacheStartupClear(NutriCacheEvictionService cacheEvictionService) {
        this.cacheEvictionService = cacheEvictionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        cacheEvictionService.clearNutritionProfileCache();
        log.info("Cleared nutritionProfile cache on startup");
    }
}
