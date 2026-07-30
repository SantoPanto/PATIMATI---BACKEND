package com.works.patimati.repository;

import com.works.patimati.entity.User;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByPhone(String phone);

    // KISIM 3
    @Query(value = "SELECT * FROM users u WHERE u.fcm_token IS NOT NULL AND ST_DWithin(u.location, :point, :distanceInMeters) = true", nativeQuery = true)
    List<User> findUsersNearby(@Param("point") Point point, @Param("distanceInMeters") double distanceInMeters);
}