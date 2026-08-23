package com.works.patimati.specification;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Complaint entities (AdComplaint, UserComplaint, AdoptionComplaint).
 * Adheres to Open/Closed Principle and Decoupled DDD tables via Criteria Subqueries.
 */
public final class ComplaintSpecification {

    private ComplaintSpecification() {
        // Utility class
    }

    /**
     * Specification for filtering AdComplaint entities by keyword.
     * Matches description, reason, associated ad title, reporter user name, or ad owner user name.
     */
    public static Specification<AdComplaint> forAd(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // 1. Complaint description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));

            // 2. Complaint reason (Enum to String)
            predicates.add(cb.like(cb.lower(root.get("reason").as(String.class)), pattern));

            // 3. Associated Ad Title
            Subquery<Long> adTitleSubquery = query.subquery(Long.class);
            Root<Ad> adRoot = adTitleSubquery.from(Ad.class);
            adTitleSubquery.select(adRoot.get("id"))
                    .where(cb.like(cb.lower(adRoot.get("title")), pattern));
            predicates.add(root.get("adId").in(adTitleSubquery));

            // 4. Reporter User Name (firstName, lastName, or full name)
            Subquery<Long> reporterSubquery = query.subquery(Long.class);
            Root<User> reporterRoot = reporterSubquery.from(User.class);
            reporterSubquery.select(reporterRoot.get("uid"))
                    .where(cb.or(
                            cb.like(cb.lower(reporterRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("lastName")), pattern),
                            cb.like(cb.lower(cb.concat(cb.concat(reporterRoot.get("firstName"), " "), reporterRoot.get("lastName"))), pattern)
                    ));
            predicates.add(root.get("reporterId").in(reporterSubquery));

            // 5. Ad Owner User Name
            Subquery<Long> adOwnerSubquery = query.subquery(Long.class);
            Root<Ad> adOwnerAdRoot = adOwnerSubquery.from(Ad.class);
            Join<Ad, User> userJoin = adOwnerAdRoot.join("user");
            adOwnerSubquery.select(adOwnerAdRoot.get("id"))
                    .where(cb.or(
                            cb.like(cb.lower(userJoin.get("firstName")), pattern),
                            cb.like(cb.lower(userJoin.get("lastName")), pattern),
                            cb.like(cb.lower(cb.concat(cb.concat(userJoin.get("firstName"), " "), userJoin.get("lastName"))), pattern)
                    ));
            predicates.add(root.get("adId").in(adOwnerSubquery));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification for filtering UserComplaint entities by keyword.
     * Matches description, reason, reporter user name, or reported user name.
     */
    public static Specification<UserComplaint> forUser(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // 1. Complaint description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));

            // 2. Complaint reason (Enum to String)
            predicates.add(cb.like(cb.lower(root.get("reason").as(String.class)), pattern));

            // 3. Reporter User Name
            Subquery<Long> reporterSubquery = query.subquery(Long.class);
            Root<User> reporterRoot = reporterSubquery.from(User.class);
            reporterSubquery.select(reporterRoot.get("uid"))
                    .where(cb.or(
                            cb.like(cb.lower(reporterRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("lastName")), pattern),
                            cb.like(cb.lower(cb.concat(cb.concat(reporterRoot.get("firstName"), " "), reporterRoot.get("lastName"))), pattern)
                    ));
            predicates.add(root.get("reporterId").in(reporterSubquery));

            // 4. Reported User Name
            Subquery<Long> reportedSubquery = query.subquery(Long.class);
            Root<User> reportedRoot = reportedSubquery.from(User.class);
            reportedSubquery.select(reportedRoot.get("uid"))
                    .where(cb.or(
                            cb.like(cb.lower(reportedRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reportedRoot.get("lastName")), pattern),
                            cb.like(cb.lower(cb.concat(cb.concat(reportedRoot.get("firstName"), " "), reportedRoot.get("lastName"))), pattern)
                    ));
            predicates.add(root.get("reportedUserId").in(reportedSubquery));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification for filtering AdoptionComplaint entities by keyword.
     * Matches description, reason, associated adoption ad title, reporter user name, or ad owner user name.
     */
    public static Specification<AdoptionComplaint> forAdoption(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // 1. Complaint description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));

            // 2. Complaint reason (Enum to String)
            predicates.add(cb.like(cb.lower(root.get("reason").as(String.class)), pattern));

            // 3. Associated Ad Title
            Subquery<Long> adTitleSubquery = query.subquery(Long.class);
            Root<Ad> adRoot = adTitleSubquery.from(Ad.class);
            adTitleSubquery.select(adRoot.get("id"))
                    .where(cb.like(cb.lower(adRoot.get("title")), pattern));
            predicates.add(root.get("adId").in(adTitleSubquery));

            // 4. Reporter User Name
            Subquery<Long> reporterSubquery = query.subquery(Long.class);
            Root<User> reporterRoot = reporterSubquery.from(User.class);
            reporterSubquery.select(reporterRoot.get("uid"))
                    .where(cb.or(
                            cb.like(cb.lower(reporterRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("lastName")), pattern),
                            cb.like(cb.lower(cb.concat(cb.concat(reporterRoot.get("firstName"), " "), reporterRoot.get("lastName"))), pattern)
                    ));
            predicates.add(root.get("reporterId").in(reporterSubquery));

            // 5. Ad Owner User Name
            Subquery<Long> adOwnerSubquery = query.subquery(Long.class);
            Root<Ad> adOwnerAdRoot = adOwnerSubquery.from(Ad.class);
            Join<Ad, User> userJoin = adOwnerAdRoot.join("user");
            adOwnerSubquery.select(adOwnerAdRoot.get("id"))
                    .where(cb.or(
                            cb.like(cb.lower(userJoin.get("firstName")), pattern),
                            cb.like(cb.lower(userJoin.get("lastName")), pattern),
                            cb.like(cb.lower(cb.concat(cb.concat(userJoin.get("firstName"), " "), userJoin.get("lastName"))), pattern)
                    ));
            predicates.add(root.get("adId").in(adOwnerSubquery));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
