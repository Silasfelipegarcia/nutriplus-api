package br.com.nutriplus.dto.response;

public record PaymentConfigResponse(
        String publicKey,
        boolean configured,
        boolean billingEnabled,
        boolean sandboxTestCards,
        boolean cardVaultMock
) {
}
