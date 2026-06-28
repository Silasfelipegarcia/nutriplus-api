package br.com.nutriplus.application.user;

import br.com.nutriplus.application.port.UserQueryPort;
import br.com.nutriplus.application.port.UserUpdatePort;
import br.com.nutriplus.application.shared.ImageThumbnailSupport;
import br.com.nutriplus.application.shared.ProfilePhotoValidator;
import br.com.nutriplus.domain.model.User;
import br.com.nutriplus.dto.request.UpdateUserProfileRequest;
import br.com.nutriplus.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserProfileUseCase {

    private final UserQueryPort userQueryPort;
    private final UserUpdatePort userUpdatePort;

    public UpdateUserProfileUseCase(UserQueryPort userQueryPort, UserUpdatePort userUpdatePort) {
        this.userQueryPort = userQueryPort;
        this.userUpdatePort = userUpdatePort;
    }

    @Transactional
    public User execute(Long userId, UpdateUserProfileRequest request) {
        User current = userQueryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String name = request.name() != null ? request.name().strip() : current.name();
        String photoUrl = request.photoUrl() != null ? request.photoUrl() : current.photoUrl();
        String photoThumbnailUrl = current.photoThumbnailUrl();

        if (request.photoUrl() != null) {
            ProfilePhotoValidator.validateOptionalPhoto(request.photoUrl());
            if (request.photoUrl().isBlank()) {
                photoUrl = null;
                photoThumbnailUrl = null;
            } else {
                photoUrl = request.photoUrl().strip();
                String thumbnail = ImageThumbnailSupport.thumbnailFromPhoto(photoUrl);
                photoThumbnailUrl = thumbnail.isBlank() ? null : thumbnail;
            }
        }

        User updated = new User(
                current.id(),
                name,
                current.email(),
                current.role(),
                current.loginEnabled(),
                current.passwordHash(),
                photoUrl,
                photoThumbnailUrl,
                current.cpfEncrypted(),
                current.failedLoginAttempts(),
                current.passwordMustChange(),
                current.termsAcceptedAt(),
                current.termsVersion(),
                current.privacyPolicyAcceptedAt(),
                current.createdAt(),
                current.updatedAt(),
                current.accessRejectedAt()
        );
        return userUpdatePort.save(updated);
    }
}
