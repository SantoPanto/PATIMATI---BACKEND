package com.works.patimati.specification;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ComplaintSpecification {

    private ComplaintSpecification() {
        // Utility class
    }

    public static Specification<AdComplaint> withAdComplaintSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // 1. Complaint description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));

            // 2. Complaint reason
            predicates.add(cb.like(cb.lower(root.get("reason").as(String.class)), pattern));

            // 3. Reporter User
            Subquery<Long> reporterSubquery = query.subquery(Long.class);
            Root<User> reporterRoot = reporterSubquery.from(User.class);
            reporterSubquery.select(reporterRoot.get("uid"));
            reporterSubquery.where(
                    cb.equal(reporterRoot.get("uid"), root.get("reporterId")),
                    cb.or(
                            cb.like(cb.lower(reporterRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("lastName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("email")), pattern)
                    )
            );
            predicates.add(cb.exists(reporterSubquery));

            // 4. Ad (title) and Ad owner (User)
            Subquery<Long> adSubquery = query.subquery(Long.class);
            Root<Ad> adRoot = adSubquery.from(Ad.class);
            Join<Ad, User> ownerJoin = adRoot.join("user", JoinType.LEFT);
            adSubquery.select(adRoot.get("id"));
            adSubquery.where(
                    cb.equal(adRoot.get("id"), root.get("adId")),
                    cb.or(
                            cb.like(cb.lower(adRoot.get("title")), pattern),
                            cb.like(cb.lower(ownerJoin.get("firstName")), pattern),
                            cb.like(cb.lower(ownerJoin.get("lastName")), pattern),
                            cb.like(cb.lower(ownerJoin.get("email")), pattern)
                    )
            );
            predicates.add(cb.exists(adSubquery));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<UserComplaint> withUserComplaintSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // 1. Complaint description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));

            // 2. Complaint reason
            predicates.add(cb.like(cb.lower(root.get("reason").as(String.class)), pattern));

            // 3. Reporter User
            Subquery<Long> reporterSubquery = query.subquery(Long.class);
            Root<User> reporterRoot = reporterSubquery.from(User.class);
            reporterSubquery.select(reporterRoot.get("uid"));
            reporterSubquery.where(
                    cb.equal(reporterRoot.get("uid"), root.get("reporterId")),
                    cb.or(
                            cb.like(cb.lower(reporterRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("lastName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("email")), pattern)
                    )
            );
            predicates.add(cb.exists(reporterSubquery));

            // 4. Reported User
            Subquery<Long> reportedUserSubquery = query.subquery(Long.class);
            Root<User> reportedUserRoot = reportedUserSubquery.from(User.class);
            reportedUserSubquery.select(reportedUserRoot.get("uid"));
            reportedUserSubquery.where(
                    cb.equal(reportedUserRoot.get("uid"), root.get("reportedUserId")),
                    cb.or(
                            cb.like(cb.lower(reportedUserRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reportedUserRoot.get("lastName")), pattern),
                            cb.like(cb.lower(reportedUserRoot.get("email")), pattern)
                    )
            );
            predicates.add(cb.exists(reportedUserSubquery));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<AdoptionComplaint> withAdoptionComplaintSearch(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            // 1. Complaint description
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));

            // 2. Complaint reason
            predicates.add(cb.like(cb.lower(root.get("reason").as(String.class)), pattern));

            // 3. Reporter User
            Subquery<Long> reporterSubquery = query.subquery(Long.class);
            Root<User> reporterRoot = reporterSubquery.from(User.class);
            reporterSubquery.select(reporterRoot.get("uid"));
            reporterSubquery.where(
                    cb.equal(reporterRoot.get("uid"), root.get("reporterId")),
                    cb.or(
                            cb.like(cb.lower(reporterRoot.get("firstName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("lastName")), pattern),
                            cb.like(cb.lower(reporterRoot.get("email")), pattern)
                    )
            );
            predicates.add(cb.exists(reporterSubquery));

            // 4. Ad (title) and Ad owner (User)
            Subquery<Long> adSubquery = query.subquery(Long.class);
            Root<Ad> adRoot = adSubquery.from(Ad.class);
            Join<Ad, User> ownerJoin = adRoot.join("user", JoinType.LEFT);
            adSubquery.select(adRoot.get("id"));
            adSubquery.where(
                    cb.equal(adRoot.get("id"), root.get("adId")),
                    cb.or(
                            cb.like(cb.lower(adRoot.get("title")), pattern),
                            cb.like(cb.lower(ownerJoin.get("firstName")), pattern),
                            cb.like(cb.lower(ownerJoin.get("lastName")), pattern),
                            cb.like(cb.lower(ownerJoin.get("email")), pattern)
                    )
            );
            predicates.add(cb.exists(adSubquery));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
