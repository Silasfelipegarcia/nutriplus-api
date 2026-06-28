package br.com.nutriplus.application.auth;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.exception.LoginDisabledException;

public final class LoginAccessPolicy {

    public static final String PENDING_MESSAGE =
            "Seu cadastro foi recebido. Aguarde a liberação do acesso para entrar no app.";

    public static final String BETA_WAITLIST_MESSAGE =
            "Recebemos sua solicitação para o beta. Estamos selecionando participantes — você receberá acesso assim que validarmos seu perfil.";

    public static final String REJECTED_MESSAGE =
            "Sua solicitação de acesso ao Nutri+ não foi aprovada neste momento. Verifique o e-mail que enviamos com mais detalhes.";

    private LoginAccessPolicy() {
    }

    public static void ensureCanLogin(User user) {
        if (user.role() == UserRole.ADMIN) {
            return;
        }
        if (user.accessRejected()) {
            throw new LoginDisabledException(REJECTED_MESSAGE);
        }
        if (!user.loginEnabled()) {
            throw new LoginDisabledException(PENDING_MESSAGE);
        }
    }

    public static String messageFor(User user) {
        if (user.role() == UserRole.ADMIN) {
            return null;
        }
        if (user.accessRejected()) {
            return REJECTED_MESSAGE;
        }
        if (!user.loginEnabled()) {
            return PENDING_MESSAGE;
        }
        return null;
    }
}
