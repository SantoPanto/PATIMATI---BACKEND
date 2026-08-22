package com.works.patimati.entity;

import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.entity.enums.Species;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "ads",
        indexes = {
                @Index(name = "idx_ads_location", columnList = "location")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private AdType adType; // LOST, FOUND, ADOPTION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String breed = "MIXED_OR_UNKNOWN";

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ad_colors",
            joinColumns = @JoinColumn(name = "ad_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "color", length = 30)
    @Builder.Default
    private Set<PetColor> colors = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PetGender gender = PetGender.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    @Builder.Default
    private AgeGroup ageGroup = AgeGroup.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "coat_pattern", nullable = false, length = 30)
    @Builder.Default
    private CoatPattern coatPattern = CoatPattern.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "collar_status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus collarStatus = PresenceStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "collar_color", length = 30)
    private PetColor collarColor;

    @Column(name = "collar_tag_text", length = 255)
    private String collarTagText;

    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", nullable = false, length = 30)
    @Builder.Default
    private EyeColor eyeColor = EyeColor.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "ear_tag_status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus earTagStatus = PresenceStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "ear_notch_status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus earNotchStatus = PresenceStatus.UNKNOWN;

    @Column(name = "microchip_number", length = 32)
    private String microchipNumber;

    @Column(name = "lost_date")
    private LocalDate lostDate;

    @Column(name = "distinctive_marks", length = 1000)
    private String distinctiveMarks;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ad_photo_urls",
            joinColumns = @JoinColumn(name = "ad_id")
    )
    @OrderColumn(name = "photo_order")
    @Column(name = "photo_url", nullable = false, length = 2048)
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    // Yeni eklenen AiStatus alanı
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status", nullable = false, length = 30)
    @Builder.Default
    private AiStatus aiStatus = AiStatus.PENDING;

    // KISIM 3: Coğrafi Konum (GIS)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    /**
     * İl/ilçe — kartlarda ham koordinat yerine gösterilir. Kayıtta form
     * beyanı öncelikli, yoksa koordinattan ters geokodlama; ikisi de
     * yoksa null kalır (V19).
     */
    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_status", nullable = false, length = 20)
    @Builder.Default
    private AdResolutionStatus resolutionStatus = AdResolutionStatus.NONE;

    /**
     * Bu kayıp ilan hangi <b>bulundu ilanıyla</b> eşleşerek kapandı.
     *
     * <p>Bilerek {@code null} olabilir: geçmiş kayıtlarda bu bilgi yok ve
     * kullanıcı eşleşen ilanı seçmeden de kapatabilir. Bağ kurulmadığında
     * {@code resolutionStatus} yine {@code FOUND} olur — yalnız hangi ilanla
     * olduğu bilinmez.
     *
     * <p><b>Neden saklıyoruz:</b> eşleşme skorundaki konum kanalının ağırlığı
     * ölçümle tartışılamıyordu, çünkü <i>gerçekte eşleşen bir çiftin arası
     * kaç km</i> sorusunun cevabı sistemde hiç yoktu. Bu alan dolmaya
     * başlayınca eşleşen çiftlerin mesafe dağılımı rastgele çiftlerinkiyle
     * karşılaştırılabilir.
     *
     * <p>İlişki {@code @ManyToOne} DEĞİL, düz kimlik: bu bağ yalnız ölçüm
     * için okunuyor, nesne grafiğine bağlamak her ilan yüklemesinde
     * gereksiz bir sorgu riski getirirdi.
     */
    @Column(name = "resolved_by_ad_id")
    private Long resolvedByAdId;

    /**
     * İlanı kapatırken bildirilen <b>bulan kullanıcı</b>.
     *
     * <p>Bu değer isteğe zaten geliyordu ({@code ResolveLostAdRequest}) ama
     * yalnız ödül puanı verilip <b>atılıyordu</b>. Saklanmadığı için "kim
     * buldu" sorusu sonradan cevaplanamıyordu.
     */
    @Column(name = "finder_user_id")
    private Long finderUserId;

    @Column(name = "suspended", nullable = false)
    @Builder.Default
    private boolean suspended = false;

    @Column(name = "is_poster_allowed", nullable = false)
    @Builder.Default
    private Boolean isPosterAllowed = false;

    @Column(name = "show_email_on_poster", nullable = false)
    @Builder.Default
    private Boolean showEmailOnPoster = false;

    @Column(name = "show_phone_on_poster", nullable = false)
    @Builder.Default
    private Boolean showPhoneOnPoster = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ---------------------------------------------------------------------
    // AI analiz sonuçları (entegrasyon sözleşmesi §6)
    // Bu alanları AI servisi doldurur; ilan CRUD'u bunlara yazmaz.
    // ---------------------------------------------------------------------

    /**
     * İlanın HER fotoğrafı için bir vektör.
     *
     * <p>Neden liste: tek fotoğrafla eşleşme oranı gerçek veride %24'te kaldı
     * (ölçüm raporu §4). Görsel skor tüm fotoğraf çiftlerinin en iyisinden
     * alınır, bu yüzden ilan başına birden çok vektör saklanır.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_embeddings", columnDefinition = "jsonb")
    private List<AiPhotoVector> aiEmbeddings;

    /** Eşleştirme skorunun %30'unu oluşturan etiketler: ["cat","tabby","brown"] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_labels", columnDefinition = "jsonb")
    private List<String> aiLabels;

    /**
     * AI'ın tür tahmini — kullanıcının beyanı olan {@link #species}'ten AYRI.
     * Sözleşme §7 kural 2: beyan önceliklidir, AI tahmini onu ezmez.
     */
    @Column(name = "ai_species", length = 16)
    private String aiSpecies;

    /**
     * Fotoğrafta gerçekten kedi/köpek görülüp görülmediği (sözleşme §4).
     *
     * <p><b>Üç durumlu, bu yüzden {@code Boolean} — {@code boolean} değil:</b>
     * {@code null} analizin henüz yapılmadığını (PENDING) ya da başarısız
     * olduğunu (FAILED) söyler, {@code FALSE} ise "bakıldı, hayvan görünmüyor"
     * demektir. İkisini aynı değere indirseydik, analizi bekleyen her ilan
     * hakkında arayüz henüz ölçülmemiş bir iddiada bulunurdu.
     *
     * <p>İlan birden çok fotoğraf taşıyabilir; AI bunlardan <b>en az biri</b>
     * hayvan içeriyorsa {@code true} döner (bkz. AI tarafında
     * {@code app/analiz.py}). Kullanıcı üç fotoğraf yüklerken birine
     * yanlışlıkla manzara koyabilir — bu ilanı reddetmek için sebep değildir.
     */
    @Column(name = "ai_is_pet")
    private Boolean aiIsPet;

    /** Bilgi amaçlı. FİLTRE DEĞİLDİR (sözleşme §7 kural 1) — melez oranı yüksek. */
    @Column(name = "ai_breed", length = 64)
    private String aiBreed;

    @Column(name = "ai_breed_confidence")
    private Float aiBreedConfidence;

    /**
     * Vektörü hangi model üretti (örn. "siglip2-animal/v2").
     *
     * <p>Model değişince eski vektörler yenileriyle kıyaslanamaz; bu alan
     * olmadan hangilerinin bayat olduğu anlaşılamaz. AI tarafı sürümü
     * tutmayan adayları eleyip raporlar.
     */
    @Column(name = "ai_model_version", length = 64)
    private String aiModelVersion;

    @Column(name = "ai_processed_at")
    private Instant aiProcessedAt;

    /**
     * Tek bir fotoğrafın vektörü ve hangi adresten üretildiği.
     *
     * <p>Adresi vektörle birlikte saklamak, kullanıcı fotoğraf sırasını
     * değiştirse bile hangi vektörün hangi kareye ait olduğunu korur.
     */
    public record AiPhotoVector(String photoUrl, float[] embedding) {
    }

    public enum AdType {
        LOST, FOUND, ADOPTION
    }
}
