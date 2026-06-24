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

    public void applyCpf(User user, String cpf) {
        String normalized = cpfProtectionService.requireValidNormalized(cpf);
        String hash = cpfProtectionService.hash(normalized);
        if (userRepository.existsByCpfHash(hash)) {
            throw new BusinessException("CPF já cadastrado");
        }
        user.setCpfHash(hash);
        user.setCpfEncrypted(cpfProtectionService.encrypt(normalized));
    }
}
