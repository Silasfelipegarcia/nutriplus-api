package br.com.nutriplus.infrastructure.persistence.jpa;

import br.com.nutriplus.infrastructure.persistence.jpa.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileJpa extends JpaRepository<UserProfileEntity, UUID> {
}