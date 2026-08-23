package com.works.patimati.specification;

import com.works.patimati.entity.Ad;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class AdSpecification {

    private AdSpecification() {
        // Utility class
    }

    public static Specification<Ad> withSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("city")), pattern),
                    cb.like(cb.lower(root.get("breed")), pattern)
            );
        };
    }
}
