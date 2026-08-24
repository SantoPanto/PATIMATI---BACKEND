package com.works.patimati.repository;

import com.works.patimati.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    // Frontend'den gelen token metnini veritabanında aramak için
    Optional<PasswordResetToken> findByToken(String token);

}