package com.works.patimati.specification;

import com.works.patimati.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * JPA Specification for User searching (Open/Closed Principle).
 */
public final class UserSpecification {

    private UserSpecification() {
        // Utility class
    }

    /**
     * Creates a specification to filter users by keyword matching firstName, lastName, or email.
     *
     * @param keyword Text to search for (LIKE %keyword%, case-insensitive)
     * @return JPA Specification for User entity
     */
    public static Specification<User> withSearch(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }
}
