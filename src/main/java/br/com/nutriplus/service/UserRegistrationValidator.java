package br.com.nutriplus.service;

import br.com.nutriplus.domain.util.AgePolicy;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserRegistrationValidator {

    private final UserRepository userRepository;
    private final CpfRegistrationService cpfRegistrationService;

    public UserRegistrationValidator(UserRepository userRepository,
                                     CpfRegistrationService cpfRegistrationService) {
        this.userRepository = userRepository;
        this.cpfRegistrationService = cpfRegistrationService;
    }

    public void validateNewPatientAccount(String email, String cpf, LocalDate birthDate) {
        ensureUniqueEmail(email);
        cpfRegistrationService.ensureCpfAvailable(cpf);
        AgePolicy.requireAdult(birthDate);
    }

    public void validateNewNutritionistAccount(String email, String cpf) {
        ensureUniqueEmail(email);
        cpfRegistrationService.ensureCpfAvailable(cpf);
    }

    private void ensureUniqueEmail(String email) {
        if (userRepository.existsByEmail(email.trim())) {
            throw new BusinessException("E-mail já cadastrado");
        }
    }
}
