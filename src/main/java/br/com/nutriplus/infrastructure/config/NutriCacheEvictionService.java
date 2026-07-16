package br.com.nutriplus.infrastructure.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class NutriCacheEvictionService {

    private final CacheManager cacheManager;

    public NutriCacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictForUser(Long userId, String... cacheNames) {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(userId);
            }
        }
    }

    public void evictMealPlanCaches(Long userId) {
        evictForUser(userId,
                NutriCacheNames.MEAL_PLAN_LATEST,
                NutriCacheNames.SHOPPING_LIST_LATEST,
                NutriCacheNames.NUTRITION_PROFILE);
    }

    public void evictCheckinCaches(Long userId) {
        Cache today = cacheManager.getCache(NutriCacheNames.CHECKINS_TODAY);
        Cache stats = cacheManager.getCache(NutriCacheNames.CHECKINS_STATS);
        Cache adherence = cacheManager.getCache(NutriCacheNames.CHECKINS_ADHERENCE);
        if (today != null) {
            today.clear();
        }
        if (stats != null) {
            stats.clear();
        }
        if (adherence != null) {
            adherence.clear();
        }
    }

    public void evictPlanTrackingCaches(Long userId) {
        evictCheckinCaches(userId);
        evictForUser(userId,
                NutriCacheNames.PROGRESS_SCHEDULE,
                NutriCacheNames.PROGRESS_MEASUREMENT_LATEST);
    }

    /** Limpa cache de perfil (ex.: após migração que cura plan_synced_at). */
    public void clearNutritionProfileCache() {
        Cache cache = cacheManager.getCache(NutriCacheNames.NUTRITION_PROFILE);
        if (cache != null) {
            cache.clear();
        }
    }
}
