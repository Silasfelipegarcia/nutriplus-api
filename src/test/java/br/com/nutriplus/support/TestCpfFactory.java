package br.com.nutriplus.support;

import br.com.nutriplus.domain.util.CpfUtil;

import java.util.concurrent.atomic.AtomicLong;

public final class TestCpfFactory {

    private static final AtomicLong SEQ = new AtomicLong(100_000_001L);

    private TestCpfFactory() {
    }

    public static String nextValidCpf() {
        for (int attempt = 0; attempt < 100; attempt++) {
            long baseNum = SEQ.getAndIncrement() % 900_000_000L + 100_000_000L;
            String base = String.format("%09d", baseNum);
            if (base.chars().distinct().count() == 1) {
                continue;
            }
            int d1 = checkDigit(base, 10);
            int d2 = checkDigit(base + d1, 11);
            String cpf = base + d1 + d2;
            if (CpfUtil.isValid(cpf)) {
                return CpfUtil.format(cpf);
            }
        }
        throw new IllegalStateException("Could not generate valid CPF for test");
    }

    private static int checkDigit(String base, int factor) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * (factor - i);
        }
        int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }
}
