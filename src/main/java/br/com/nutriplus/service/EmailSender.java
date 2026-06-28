package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.UserRole;

public interface EmailSender {

    void sendPasswordReset(String email, String name, String resetLink);

    void sendBetaAccessApproved(String email, String name, String loginLink, UserRole role);
}
