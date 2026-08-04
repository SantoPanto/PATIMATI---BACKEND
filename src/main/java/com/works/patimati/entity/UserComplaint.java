package com.works.patimati.entity;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Kullanıcı Profil Şikayeti Entity'si (User Complaint Domain Entity).
 * 
 * DDD (Domain-Driven Design) ve Decoupled Tables prensiplerine uygun olarak:
 * 1. Veritabanı tablosu 'user_complaints' olarak ayrılmıştır.
 * 2. Kullanıcı (User) modülü ile sınırları korumak adına JPA entity ilişkisi yerine primitive 'reporterId' ve 'reportedUserId' alanları tercih edilmiştir.
 */
@Entity
@Table(
        name = "user_complaints",
        indexes = {
                @Index(name = "idx_user_complaint_reporter_user_status", columnList = "reporter_id, reported_user_id, status"),
                @Index(name = "idx_user_complaint_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Şikayet eden kullanıcının benzersiz kimlik numarası (uid).
     */
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    /**
     * Şikayet edilen kullanıcının benzersiz kimlik numarası (uid).
     */
    @Column(name = "reported_user_id", nullable = false)
    private Long reportedUserId;

    /**
     * Şikayet sebebi (Enum).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintReason reason;

    /**
     * Şikayet açıklaması / detay metni.
     */
    @Column(length = 1000, nullable = false)
    private String description;

    /**
     * Şikayet durumu (BEKLEMEDE, INCELEMEDE, COZULDU vb.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.BEKLEMEDE;

    /**
     * Şikayet kayıt tarihi.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
