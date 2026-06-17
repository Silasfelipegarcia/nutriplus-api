package br.com.nutriplus.application.user;

import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.shared.ActingUserResolver;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserUseCase {

    private final ActingUserResolver actingUserResolver;
    private final UserQueryPort userQueryPort;

    public GetCurrentUserUseCase(ActingUserResolver actingUserResolver, UserQueryPort userQueryPort) {
        this.actingUserResolver = actingUserResolver;
        this.userQueryPort = userQueryPort;
    }

    public User execute() {
        Long userId = actingUserResolver.resolveUserId();
        return userQueryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
