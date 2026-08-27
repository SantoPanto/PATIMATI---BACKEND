package com.works.patimati.repository;

import com.works.patimati.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    List<User> findAllByRole(User.Role role);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndEmailNot(String phone, String email);

    // Eski findUsersNearby (aboneliksiz yakındaki-kullanıcı bildirimi) buradan
    // kaldırıldı; yerini AlertSubscriptionRepository.findEnabledWithinOwnRadius
    // aldı. Oradaki PARAMETREDE-::geography-YAZILAMAZ uyarısı bu sorguda
    // yaşanmış gerçek bir hatadan geliyordu.
}
