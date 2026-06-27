package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.infrastructure.security.CpfProtectionService;
import br.com.nutriplus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CpfRegistrationService {

    private final UserRepository userRepository;
    private final CpfProtectionService cpfProtectionService;

    public CpfRegistrationService(UserRepository userRepository, CpfProtectionService cpfProtectionService) {
        this.userRepository = userRepository;
        this.cpfProtectionService = cpfProtectionService;
    }

    public void ensureCpfAvailable(String cpf) {
        String normalized = cpfProtectionService.requireValidNormalized(cpf);
        assertHashAvailable(cpfProtectionService.hash(normalized));
    }

    public void applyCpf(User user, String cpf) {
        String normalized = cpfProtectionService.requireValidNormalized(cpf);
        String hash = cpfProtectionService.hash(normalized);
        assertHashAvailable(hash);
        user.setCpfHash(hash);
        user.setCpfEncrypted(cpfProtectionService.encrypt(normalized));
    }

    private void assertHashAvailable(String hash) {
        if (userRepository.existsByCpfHash(hash)) {
            throw new BusinessException("CPF já cadastrado");
        }
    }
}
