package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.NutritionProfile;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.enums.AdminUserAccessStatus;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;

public final class AdminUserSpecifications {

    private AdminUserSpecifications() {
    }

    public static Specification<User> withAccessStatus(AdminUserAccessStatus status) {
        if (status == null || status == AdminUserAccessStatus.ALL) {
            return null;
        }
        return switch (status) {
            case PENDING -> (root, query, cb) -> cb.and(
                    cb.isFalse(root.get("loginEnabled")),
                    cb.isNull(root.get("accessRejectedAt")));
            case APPROVED -> (root, query, cb) -> cb.isTrue(root.get("loginEnabled"));
            case REJECTED -> (root, query, cb) -> cb.isNotNull(root.get("accessRejectedAt"));
            case ALL -> null;
        };
    }

    public static Specification<User> withRole(UserRole role) {
        if (role == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> withRegistrationSource(RegistrationSource source) {
        if (source == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("registrationSource"), source);
    }

    public static Specification<User> withNutritionProfile(Boolean hasProfile) {
        if (hasProfile == null) {
            return null;
        }
        return (root, query, cb) -> {
            var subquery = query.subquery(Long.class);
            var profile = subquery.from(NutritionProfile.class);
            subquery.select(cb.literal(1L)).where(cb.equal(profile.get("user"), root));
            return hasProfile ? cb.exists(subquery) : cb.not(cb.exists(subquery));
        };
    }

    public static Specification<User> withSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("email")), pattern));
    }

    @SafeVarargs
    public static Specification<User> combine(Specification<User>... specs) {
        Specification<User> combined = Specification.where(null);
        if (specs == null) {
            return combined;
        }
        for (Specification<User> spec : specs) {
            if (spec != null) {
                combined = combined.and(spec);
            }
        }
        return combined;
    }
}
