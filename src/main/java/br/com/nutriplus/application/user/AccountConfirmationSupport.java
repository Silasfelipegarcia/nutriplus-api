package br.com.nutriplus.application.user;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.application.port.PasswordHasherPort;
import br.com.nutriplus.dto.request.DeleteAccountRequest;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.InvalidCredentialsException;

final class AccountConfirmationSupport {

    private AccountConfirmationSupport() {
    }

    static void verifyPassword(User user, String currentPassword, PasswordHasherPort passwordHasherPort) {
        if (!passwordHasherPort.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Senha atual incorreta.");
        }
    }

    static void verifyEmailConfirmation(String email, String confirmation, String actionLabel) {
        if (!email.trim().equalsIgnoreCase(confirmation.trim())) {
            throw new BusinessException(
                    "Digite seu e-mail exatamente como cadastrado para confirmar " + actionLabel + ".");
        }
    }

    static void verifyPasswordAndEmail(User user,
                                       DeleteAccountRequest request,
                                       PasswordHasherPort passwordHasherPort,
                                       String actionLabel) {
        verifyPassword(user, request.currentPassword(), passwordHasherPort);
        verifyEmailConfirmation(user.getEmail(), request.emailConfirmation(), actionLabel);
    }
}
