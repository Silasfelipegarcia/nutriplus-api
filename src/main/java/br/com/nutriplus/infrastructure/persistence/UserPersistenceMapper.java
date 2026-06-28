package br.com.nutriplus.infrastructure.persistence;

final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    static br.com.nutriplus.domain.model.User toDomain(br.com.nutriplus.domain.entity.User entity) {
        return new br.com.nutriplus.domain.model.User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.isLoginEnabled(),
                entity.getPasswordHash(),
                entity.getPhotoUrl(),
                entity.getPhotoThumbnailUrl(),
                entity.getCpfEncrypted(),
                entity.getFailedLoginAttempts(),
                entity.isPasswordMustChange(),
                entity.getTermsAcceptedAt(),
                entity.getTermsVersion(),
                entity.getPrivacyPolicyAcceptedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getAccessRejectedAt()
        );
    }

    static void applyDomain(br.com.nutriplus.domain.model.User domain, br.com.nutriplus.domain.entity.User entity) {
        entity.setName(domain.name());
        entity.setEmail(domain.email());
        entity.setPasswordHash(domain.passwordHash());
        entity.setPhotoUrl(domain.photoUrl());
        entity.setPhotoThumbnailUrl(domain.photoThumbnailUrl());
        entity.setFailedLoginAttempts(domain.failedLoginAttempts());
        entity.setPasswordMustChange(domain.passwordMustChange());
    }

    static br.com.nutriplus.domain.entity.User toNewEntity(br.com.nutriplus.domain.model.User domain) {
        return br.com.nutriplus.domain.entity.User.builder()
                .name(domain.name())
                .email(domain.email())
                .role(domain.role())
                .loginEnabled(domain.loginEnabled())
                .passwordHash(domain.passwordHash())
                .photoUrl(domain.photoUrl())
                .photoThumbnailUrl(domain.photoThumbnailUrl())
                .failedLoginAttempts(domain.failedLoginAttempts())
                .passwordMustChange(domain.passwordMustChange())
                .build();
    }
}
