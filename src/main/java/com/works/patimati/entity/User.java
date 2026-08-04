package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point; 

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
    private Long uid;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 255)
    private String password;

    @Column(unique = true, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "fcm_token")
    private String fcmToken;

    // Hesabı kapatıp açmak için kontrol
    private boolean enabled;

    // KISIM 3: Coğrafi Konum
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // Rol yönetimi için Enum tanımı
    public enum Role {
        GUEST, USER, ADMIN
    }
}
