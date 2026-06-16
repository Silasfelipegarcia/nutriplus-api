package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final ResponseMapper responseMapper;

    public UserResponse getMe() {
        User user = currentUser.get();
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.getId()).isPresent();
        return responseMapper.toUserResponse(user, hasProfile);
    }
}
