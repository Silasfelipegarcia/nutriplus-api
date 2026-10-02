package br.com.nutriplus.mapper;

import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.infrastructure.security.CpfProtectionService;
import br.com.nutriplus.service.HealthReferenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class ResponseMapperCpfDisplayTest {

    private static final String TEST_KEY = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Test
    void toUserResponseDoesNotThrowWhenCpfDecryptFails() {
        CpfProtectionService cpfProtectionService = new CpfProtectionService(TEST_KEY);
        ResponseMapper mapper = new ResponseMapper(
                new ObjectMapper(), mock(HealthReferenceService.class), cpfProtectionService);
        User user = new User(
                18L, "Ana", "ana@nutriplus.test", UserRole.NUTRITIONIST, true, "hash",
                null, null, "not-valid-ciphertext", 0, false,
                null, null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null, null);

        assertThatCode(() -> mapper.toUserResponse(user, false)).doesNotThrowAnyException();
        assertThat(mapper.toUserResponse(user, false).cpfMasked()).isNull();
        assertThat(mapper.toUserResponse(user, false).name()).isEqualTo("Ana");
    }

    @Test
    void toUserResponseDoesNotThrowWhenCiphertextWasEncryptedWithAnotherKey() {
        CpfProtectionService otherKey = new CpfProtectionService("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");
        String ciphertext = otherKey.encrypt("52998224725");
        CpfProtectionService cpfProtectionService = new CpfProtectionService(TEST_KEY);
        ResponseMapper mapper = new ResponseMapper(
                new ObjectMapper(), mock(HealthReferenceService.class), cpfProtectionService);
        br.com.nutriplus.domain.entity.User entity = br.com.nutriplus.domain.entity.User.builder()
                .id(18L)
                .name("Ana")
                .email("ana@nutriplus.test")
                .role(UserRole.NUTRITIONIST)
                .build();
        entity.setCpfEncrypted(ciphertext);

        assertThatCode(() -> mapper.toUserResponse(entity, false)).doesNotThrowAnyException();
        assertThat(mapper.toUserResponse(entity, false).cpfMasked()).isNull();
    }
}
