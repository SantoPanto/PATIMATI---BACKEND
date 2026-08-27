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

    /**
     * Bir ilceye atanmis kurum (belediye) hesaplari -- yeni ihbar bildirimi
     * bunlara duser.
     *
     * <p>IgnoreCase kiyasi SQL'in KENDI motorunda yapilir. Java'nin Turkce
     * yerelindeki {@code toLowerCase()} ile PostgreSQL'in {@code lower()}'i
     * ayni sonucu vermeyebiliyor; ilce adi iki tarafta ayri ayri
     * kucultulurse ayni ilce eslesmeyebilir
     * (bkz. {@code MunicipalityScopeService} sinif notu). Ihbar sorgulari
     * da ayni sebeple JPQL icinde LOWER kullaniyor.
     */
    List<User> findByRoleAndInstitutionDistrictIgnoreCase(User.Role role, String institutionDistrict);

    // Eski findUsersNearby (aboneliksiz yakındaki-kullanıcı bildirimi) buradan
    // kaldırıldı; yerini AlertSubscriptionRepository.findEnabledWithinOwnRadius
    // aldı. Oradaki PARAMETREDE-::geography-YAZILAMAZ uyarısı bu sorguda
    // yaşanmış gerçek bir hatadan geliyordu.
}
