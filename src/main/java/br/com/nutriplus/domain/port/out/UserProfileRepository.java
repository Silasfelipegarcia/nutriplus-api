package br.com.nutriplus.domain.port.out;
import br.com.nutriplus.domain.model.UserProfile;
import java.util.Optional; import java.util.UUID;
public interface UserProfileRepository {
  UserProfile save(UserProfile userProfile);
  Optional<UserProfile> findById(UUID userId);
}