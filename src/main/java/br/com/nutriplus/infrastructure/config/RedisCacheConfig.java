package br.com.nutriplus.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "nutriplus.cache")
public class RedisCacheConfig {

    private boolean enabled;
    private long mealPlanTtlSeconds = 60;
    private long nutritionProfileTtlSeconds = 120;

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

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
