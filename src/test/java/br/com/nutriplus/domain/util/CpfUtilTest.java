package br.com.nutriplus.domain.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfUtilTest {

    @Test
    void validatesKnownCpf() {
        assertThat(CpfUtil.isValid("529.982.247-25")).isTrue();
    }

    @Test
    void rejectsRepeatedDigits() {
        assertThat(CpfUtil.isValid("111.111.111-11")).isFalse();
    }

    @Test
    void masksCpf() {
        assertThat(CpfUtil.mask("52998224725")).isEqualTo("***.982.247-**");
    }
}
