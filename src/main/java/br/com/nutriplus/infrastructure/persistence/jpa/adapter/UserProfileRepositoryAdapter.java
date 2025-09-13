package br.com.nutriplus.infrastructure.persistence.jpa.adapter;

import br.com.nutriplus.domain.model.UserProfile;
import br.com.nutriplus.domain.port.out.UserProfileRepository;
import br.com.nutriplus.infrastructure.persistence.jpa.UserProfileJpa;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.UserProfileEntity;
import br.com.nutriplus.infrastructure.persistence.jpa.mapper.UserProfileMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserProfileRepositoryAdapter implements UserProfileRepository {
    private final UserProfileJpa userProfileJpa;

    public UserProfileRepositoryAdapter(UserProfileJpa userProfileJpa) {
        this.userProfileJpa = userProfileJpa;
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileEntity entity = UserProfileMapper.toEntity(userProfile);
        UserProfileEntity saved = userProfileJpa.save(entity);
        return UserProfileMapper.toDomain(saved);
    }

    @Override
    public Optional<UserProfile> findById(UUID userId) {
        return userProfileJpa.findById(userId).map(UserProfileMapper::toDomain);
    }
}