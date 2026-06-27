package br.com.nutriplus.support;

import br.com.nutriplus.domain.util.CpfUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestCpfFactoryTest {

    @Test
    void generatesUniqueValidCpfs() {
        String first = TestCpfFactory.nextValidCpf();
        String second = TestCpfFactory.nextValidCpf();

        assertThat(CpfUtil.isValid(first)).isTrue();
        assertThat(CpfUtil.isValid(second)).isTrue();
        assertThat(first).isNotEqualTo(second);
    }
}
