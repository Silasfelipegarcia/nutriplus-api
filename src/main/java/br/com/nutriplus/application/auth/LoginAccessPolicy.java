package br.com.nutriplus.application.auth;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.LoginDisabledException;

public final class LoginAccessPolicy {

    public static final String PENDING_MESSAGE =
            "Seu cadastro foi recebido. Aguarde a liberação do acesso para entrar no app.";

    public static final String BETA_WAITLIST_MESSAGE =
            "Recebemos sua solicitação para o beta. Estamos selecionando participantes — você receberá acesso assim que validarmos seu perfil.";

    private LoginAccessPolicy() {
    }

    public static void ensureCanLogin(User user) {
        if (user.role() == UserRole.ADMIN) {
            return;
        }
        if (!user.loginEnabled()) {
            throw new LoginDisabledException(PENDING_MESSAGE);
        }
    }
}
