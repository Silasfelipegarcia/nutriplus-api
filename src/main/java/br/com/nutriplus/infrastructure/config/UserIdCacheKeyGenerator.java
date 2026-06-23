package br.com.nutriplus.infrastructure.config;

import br.com.nutriplus.security.CurrentUser;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("userIdCacheKeyGenerator")
public class UserIdCacheKeyGenerator implements KeyGenerator {

    private final CurrentUser currentUser;

    public UserIdCacheKeyGenerator(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return currentUser.get().getId();
    }
}
