package br.com.nutriplus.infrastructure.config;

import br.com.nutriplus.security.CurrentUser;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "nutriplus.cache")
public class RedisCacheConfig {

    private boolean enabled;
    private long mealPlanTtlSeconds = 60;
    private long nutritionProfileTtlSeconds = 120;
    private long shoppingListTtlSeconds = 60;
    private long checkinsTtlSeconds = 30;
    private long progressTtlSeconds = 60;
    private long userMeTtlSeconds = 60;
    private long staticContentTtlSeconds = 3600;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMealPlanTtlSeconds() {
        return mealPlanTtlSeconds;
    }

    public void setMealPlanTtlSeconds(long mealPlanTtlSeconds) {
        this.mealPlanTtlSeconds = mealPlanTtlSeconds;
    }

    public long getNutritionProfileTtlSeconds() {
        return nutritionProfileTtlSeconds;
    }

    public void setNutritionProfileTtlSeconds(long nutritionProfileTtlSeconds) {
        this.nutritionProfileTtlSeconds = nutritionProfileTtlSeconds;
    }

    public long getShoppingListTtlSeconds() {
        return shoppingListTtlSeconds;
    }

    public void setShoppingListTtlSeconds(long shoppingListTtlSeconds) {
        this.shoppingListTtlSeconds = shoppingListTtlSeconds;
    }

    public long getCheckinsTtlSeconds() {
        return checkinsTtlSeconds;
    }

    public void setCheckinsTtlSeconds(long checkinsTtlSeconds) {
        this.checkinsTtlSeconds = checkinsTtlSeconds;
    }

    public long getProgressTtlSeconds() {
        return progressTtlSeconds;
    }

    public void setProgressTtlSeconds(long progressTtlSeconds) {
        this.progressTtlSeconds = progressTtlSeconds;
    }

    public long getUserMeTtlSeconds() {
        return userMeTtlSeconds;
    }

    public void setUserMeTtlSeconds(long userMeTtlSeconds) {
        this.userMeTtlSeconds = userMeTtlSeconds;
    }

    public long getStaticContentTtlSeconds() {
        return staticContentTtlSeconds;
    }

    public void setStaticContentTtlSeconds(long staticContentTtlSeconds) {
        this.staticContentTtlSeconds = staticContentTtlSeconds;
    }

    @Bean
    @ConditionalOnProperty(name = "nutriplus.cache.enabled", havingValue = "false", matchIfMissing = true)
    CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    @ConditionalOnProperty(name = "nutriplus.cache.enabled", havingValue = "true")
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    CacheManager caffeineCacheManager() {
        List<Cache> caches = new ArrayList<>();
        caches.add(caffeineCache(NutriCacheNames.USER_ME, userMeTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.NUTRITION_PROFILE, nutritionProfileTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.MEAL_PLAN_LATEST, mealPlanTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.SHOPPING_LIST_LATEST, shoppingListTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.CHECKINS_TODAY, checkinsTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.CHECKINS_STATS, checkinsTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.PROGRESS_SCHEDULE, progressTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.PROGRESS_MEASUREMENT_LATEST, progressTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.SPORT_CATALOG, staticContentTtlSeconds));
        caches.add(caffeineCache(NutriCacheNames.LEGAL_DOCUMENTS, staticContentTtlSeconds));

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }

    private static CaffeineCache caffeineCache(String name, long ttlSeconds) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .recordStats()
                .build());
    }

    @Bean
    @ConditionalOnProperty(name = "nutriplus.cache.enabled", havingValue = "true")
    @ConditionalOnBean(RedisConnectionFactory.class)
    CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> perCache = Map.ofEntries(
                Map.entry(NutriCacheNames.USER_ME, defaults.entryTtl(Duration.ofSeconds(userMeTtlSeconds))),
                Map.entry(NutriCacheNames.NUTRITION_PROFILE,
                        defaults.entryTtl(Duration.ofSeconds(nutritionProfileTtlSeconds))),
                Map.entry(NutriCacheNames.MEAL_PLAN_LATEST, defaults.entryTtl(Duration.ofSeconds(mealPlanTtlSeconds))),
                Map.entry(NutriCacheNames.SHOPPING_LIST_LATEST,
                        defaults.entryTtl(Duration.ofSeconds(shoppingListTtlSeconds))),
                Map.entry(NutriCacheNames.CHECKINS_TODAY, defaults.entryTtl(Duration.ofSeconds(checkinsTtlSeconds))),
                Map.entry(NutriCacheNames.CHECKINS_STATS, defaults.entryTtl(Duration.ofSeconds(checkinsTtlSeconds))),
                Map.entry(NutriCacheNames.PROGRESS_SCHEDULE, defaults.entryTtl(Duration.ofSeconds(progressTtlSeconds))),
                Map.entry(NutriCacheNames.PROGRESS_MEASUREMENT_LATEST,
                        defaults.entryTtl(Duration.ofSeconds(progressTtlSeconds))),
                Map.entry(NutriCacheNames.SPORT_CATALOG,
                        defaults.entryTtl(Duration.ofSeconds(staticContentTtlSeconds))),
                Map.entry(NutriCacheNames.LEGAL_DOCUMENTS,
                        defaults.entryTtl(Duration.ofSeconds(staticContentTtlSeconds)))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults.entryTtl(Duration.ofSeconds(nutritionProfileTtlSeconds)))
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
