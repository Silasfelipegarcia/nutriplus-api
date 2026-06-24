package br.com.nutriplus.domain.util;

public final class CpfUtil {

    private CpfUtil() {
    }

    public static String normalize(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    public static boolean isValid(String cpf) {
        String digits = normalize(cpf);
        if (digits.length() != 11) {
            return false;
        }
        if (digits.chars().distinct().count() == 1) {
            return false;
        }
        int d1 = checkDigit(digits.substring(0, 9), 10);
        int d2 = checkDigit(digits.substring(0, 10), 11);
        return digits.charAt(9) == ('0' + d1) && digits.charAt(10) == ('0' + d2);
    }

    private static int checkDigit(String base, int factor) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * (factor - i);
        }
        int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }

    public static String mask(String normalizedCpf) {
        String digits = normalize(normalizedCpf);
        if (digits.length() != 11) {
            return null;
        }
        return "***." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-**";
    }

    public static String format(String normalizedCpf) {
        String digits = normalize(normalizedCpf);
        if (digits.length() != 11) {
            return digits;
        }
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
    }
}
