package br.com.nutriplus.infrastructure.config;

import br.com.nutriplus.security.CurrentUser;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;

@Component("userDateCacheKeyGenerator")
public class UserDateCacheKeyGenerator implements KeyGenerator {

    private final CurrentUser currentUser;

    public UserDateCacheKeyGenerator(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return LocalDate.now() + "-" + currentUser.get().getId();
    }
}
