package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.UserRole;

public interface EmailSender {

    void sendPasswordReset(String email, String name, String resetLink);

    void sendBetaAccessApproved(String email, String name, String loginLink, UserRole role);

    void sendBetaAccessRejected(String email, String name, String reason, UserRole role);

    void sendNutritionistVerificationRejected(String email, String name, String reason);

    void sendTestEmail(String email, String name);

    void sendCareExpiredRatingPrompt(String email, String patientName, String nutritionistName);

    void sendHouseholdPlanInvitation(String email, String inviteeName, String inviterName, String inviteUrl, int expiryDays);
}
