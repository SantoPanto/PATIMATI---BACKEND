package com.works.patimati.repository;

import com.works.patimati.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndEmailNot(String phone, String email);

    // Eski findUsersNearby (aboneliksiz yakındaki-kullanıcı bildirimi) buradan
    // kaldırıldı; yerini AlertSubscriptionRepository.findEnabledWithinOwnRadius
    // aldı. Oradaki PARAMETREDE-::geography-YAZILAMAZ uyarısı bu sorguda
    // yaşanmış gerçek bir hatadan geliyordu.
}
