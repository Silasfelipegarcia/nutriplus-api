package br.com.nutriplus.infrastructure.config;

import br.com.nutriplus.security.CurrentUser;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;

@Component("userDaysCacheKeyGenerator")
public class UserDaysCacheKeyGenerator implements KeyGenerator {

    private final CurrentUser currentUser;

    public UserDaysCacheKeyGenerator(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        int days = params.length > 0 && params[0] instanceof Integer i ? i : 7;
        return LocalDate.now() + "-" + currentUser.get().getId() + "-" + days;
    }
}
