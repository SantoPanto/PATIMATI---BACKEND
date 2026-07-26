package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point; // Импортируем Point из JTS (3-я часть)

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "fcm_token")
    private String fcmToken;

    // KISIM 3
    @Column(columnDefinition = "geography(Point, 4326)")
    private Point location;

    // Rol yönetimi için Enum tanımı
    public enum Role {
        GUEST, USER, ADMIN
    }
}