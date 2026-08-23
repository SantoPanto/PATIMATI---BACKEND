package com.works.patimati.specification;

import com.works.patimati.entity.Ad;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * JPA Specification for Ad searching (Open/Closed Principle).
 */
public final class AdSpecification {

    private AdSpecification() {
        // Utility class
    }

    /**
     * Creates a specification to filter ads by keyword matching title, description, city, or breed.
     *
     * @param keyword Text to search for (LIKE %keyword%, case-insensitive)
     * @return JPA Specification for Ad entity
     */
    public static Specification<Ad> withSearch(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("city")), pattern),
                    cb.like(cb.lower(root.get("breed")), pattern)
            );
        };
    }
}
