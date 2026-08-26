package com.works.patimati.entity;

import com.works.patimati.entity.enums.VetCustomerRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Bir kullanıcının bir veterinere gönderdiği "müşteri isteği". Çift
 * (requester, vet) başına tek satır (UNIQUE, V31) -- reddedilirse aynı
 * satır tekrar PENDING'e döner (yeniden istek), ayrı bir geçmiş tutulmaz.
 * {@code status=ACCEPTED} olan satırlar, vet'in "Müşterilerim" listesinin
 * kendisidir -- ayrı bir müşteri tablosu YOK.
 */
@Entity
@Table(name = "vet_customer_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetCustomerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_id", nullable = false)
    private User vet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VetCustomerRequestStatus status = VetCustomerRequestStatus.PENDING;

    // AlertSubscription/VetClinic'ten FARKLI olarak bilerek CreationTimestamp
    // (istemci taraflı) kullanılıyor -- bu alan sendRequest()'in HEMEN
    // döndürdüğü yanıtta gösteriliyor; DB varsayılanına (insertable=false)
    // bırakılsaydı ilk yanıtta null görünürdü (satır DB'den yeniden
    // okunmadan), canlı testte yakalandı.
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;
}
