package br.com.nutriplus.service;

import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CpfRegistrationService cpfRegistrationService;

    @InjectMocks
    private UserRegistrationValidator validator;

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateNewPatientAccount(
                "user@test.com", "529.982.247-25", LocalDate.of(1990, 1, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail já cadastrado");
    }

    @Test
    void rejectsDuplicateCpf() {
        doThrow(new BusinessException("CPF já cadastrado"))
                .when(cpfRegistrationService)
                .ensureCpfAvailable("529.982.247-25");

        assertThatThrownBy(() -> validator.validateNewPatientAccount(
                "user@test.com", "529.982.247-25", LocalDate.of(1990, 1, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("CPF já cadastrado");
    }

    @Test
    void acceptsUniqueEmailAndCpf() {
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);

        assertThatCode(() -> validator.validateNewPatientAccount(
                "user@test.com", "529.982.247-25", LocalDate.of(1990, 1, 1)))
                .doesNotThrowAnyException();

        verify(cpfRegistrationService).ensureCpfAvailable("529.982.247-25");
    }
}
