package com.works.patimati.entity;

import com.works.patimati.entity.enums.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * "Kime bildirilecek" ve "o kullanıcının kararı ne" — {@link PotentialMatch}
 * kimliğinden AYRI (Faz 2 revize blueprint §3). Native↔native bir eşleşmede
 * iki bağımsız satır vardır (bir sahip CONFIRMED derken diğeri PENDING
 * kalabilir); native↔external'da tek satır vardır.
 *
 * <p>Bu satır aynı zamanda bildirim teslimatının <b>dayanıklı niyet
 * kaydıdır</b> — {@code sendAttempts} ve durable {@code status} sayesinde
 * ayrı bir outbox tablosuna gerek kalmaz (Faz 2 revize blueprint §4).
 */
@Entity
@Table(name = "potential_match_recipients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotentialMatchRecipient {

    public enum Role {
        OWNER_A,
        OWNER_B,
        OWNER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "potential_match_id", nullable = false)
    private PotentialMatch potentialMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MatchStatus status = MatchStatus.PENDING;

    @Column(name = "send_attempts", nullable = false)
    @Builder.Default
    private short sendAttempts = 0;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "viewed_at")
    private Instant viewedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
