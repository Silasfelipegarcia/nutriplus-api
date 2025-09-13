package br.com.nutriplus.infrastructure.persistence.jpa.mapper;

import br.com.nutriplus.domain.model.UserProfile;
import br.com.nutriplus.infrastructure.persistence.jpa.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class UserProfileMapper {
    private UserProfileMapper() {
    }

    public static UserProfileEntity toEntity(UserProfile domain) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setId(domain.getId() == null ? UUID.randomUUID() : domain.getId());
        entity.setName(domain.getName());
        entity.setSex(domain.getSex());
        entity.setAge(domain.getAge());
        entity.setHeightCm(domain.getHeightCm());
        entity.setWeightKg(domain.getWeightKg());
        entity.setTrainingDaysPerWeek(domain.getTrainingDaysPerWeek());
        entity.setDislikes(domain.getDislikes() == null ? null : String.join(",", domain.getDislikes()));
        entity.setStyle(domain.getStyle() == null ? null : String.join(",", domain.getStyle()));
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }

    public static UserProfile toDomain(UserProfileEntity entity) {
        List<String> dislikes = entity.getDislikes() == null ? null : Arrays.asList(entity.getDislikes().split(","));
        List<String> style = entity.getStyle() == null ? null : Arrays.asList(entity.getStyle().split(","));
        return UserProfile.builder().id(entity.getId()).name(entity.getName()).sex(entity.getSex()).age(entity.getAge()).heightCm(entity.getHeightCm()).weightKg(entity.getWeightKg()).trainingDaysPerWeek(entity.getTrainingDaysPerWeek()).dislikes(dislikes).style(style).build();
    }
}