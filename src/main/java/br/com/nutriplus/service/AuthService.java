package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.LoginRequest;
import br.com.nutriplus.dto.request.RegisterRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.mapper.ResponseMapper;
import br.com.nutriplus.repository.NutritionProfileRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ResponseMapper responseMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(responseMapper.toUserResponse(user, false))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas"));

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        boolean hasProfile = nutritionProfileRepository.findByUserId(user.getId()).isPresent();

        return AuthResponse.builder()
                .token(token)
                .user(responseMapper.toUserResponse(user, hasProfile))
                .build();
    }
}
