package br.com.nutriplus.domain.util;

import br.com.nutriplus.exception.BusinessException;

public final class ContactPhoneNormalizer {

    private ContactPhoneNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("Informe um telefone para contato.");
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("55") && digits.length() >= 12) {
            digits = digits.substring(2);
        }
        if (digits.length() < 10 || digits.length() > 11) {
            throw new BusinessException("Informe um telefone válido com DDD.");
        }
        return digits;
    }
}
