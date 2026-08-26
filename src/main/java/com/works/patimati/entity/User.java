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
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "lost_points", nullable = false)
    private int lostPoints = 0;

    @Builder.Default
    @Column(name = "adoption_points", nullable = false)
    private int adoptionPoints = 0;

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt;

    // KISIM 3: Coğrafi Konum
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // --- Kurum (belediye) hesabı alanları, V34 ---
    // Üçü de NULL olabilir: normal kullanıcı satırlarında boş kalır.
    // Doluluk yalnız role = INSTITUTION için beklenir ve orada da
    // kapsam çözücü ilçeyi bulamazsa isteği reddeder (sessizce
    // "hepsini göster"e düşmek, bir belediyeye tüm ülkeyi açardı).

    @Column(name = "institution_name", length = 150)
    private String institutionName;

    @Column(name = "institution_city", length = 100)
    private String institutionCity;

    /**
     * Panelin kapsamı. ads.district ile kıyaslanacağı için ReverseGeocodingService'in
     * ürettiği yazımla ("Nilüfer") saklanır; kıyas büyük/küçük harf duyarsızdır.
     */
    @Column(name = "institution_district", length = 100)
    private String institutionDistrict;

    // Rol yönetimi için Enum tanımı
    public enum Role {
        GUEST, USER, ADMIN, VET, PETSHOP, BARINAK,
        /**
         * Belediye/kurum hesabı (V34). Ayrı bir tablo değil, bu satırın
         * kendisi: rol INSTITUTION olduğunda aşağıdaki institution* alanları
         * dolar ve {@code /api/municipality/**} uçları açılır. Kuruma
         * yükseltme yalnız yöneticiden yapılır — serbest kayıt yok, çünkü
         * kendini belediye ilan eden bir hesap başkasının verisini okurdu.
         */
        INSTITUTION
    }
}
