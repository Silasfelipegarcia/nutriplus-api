package br.com.nutriplus.dto.response;

public record AdminEmailTestResponse(
        boolean sent,
        String recipient,
        boolean resendConfigured,
        String message
) {
}
