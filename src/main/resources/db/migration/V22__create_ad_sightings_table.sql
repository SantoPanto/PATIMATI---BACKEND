-- "Gördüm" bildirimi: üçüncü kişi, ilan açmadan kayıp ilanına konum +
-- opsiyonel foto + not bırakır; ilan sahibine bildirim gider ve görülmeler
-- ilan detayında YALNIZ sahibine listelenir (ürün kararı 22.08).
--
-- reporter_id NULLABLE: bildirimi girişsiz ziyaretçi de bırakabilir (afişteki
-- QR'ı okutan kişi üye değildir — ürün kararı). Girişsiz erişimin bedeli
-- reporter_contact'ın ZORUNLU olması: sahibi bildirene ulaşabilmeli.
-- V13 (adoption_complaints) bu yüzden kopyalanamadı: orada reporter NOT NULL.
--
-- location'a GIST indeksi BİLEREK yok: bu tabloya yarıçap sorgusu yapılmıyor,
-- görülmeler yalnız ilana bağlı listelenir (aşağıdaki bileşik indeks).
-- Harita çizimi ön yüzde, satırların koordinatlarıyla yapılır.
CREATE TABLE ad_sightings (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    reporter_id BIGINT,
    reporter_contact VARCHAR(255) NOT NULL,
    note TEXT,
    photo_url VARCHAR(2048),
    location geometry(Point, 4326) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ad_sightings_ad
        FOREIGN KEY (ad_id)
        REFERENCES ads(id)
        ON DELETE CASCADE,

    -- Hesap silinirse görülme kalır (sahibi için değerli saha bilgisi),
    -- yalnız bırakanla bağı kopar.
    CONSTRAINT fk_ad_sightings_reporter
        FOREIGN KEY (reporter_id)
        REFERENCES users(uid)
        ON DELETE SET NULL
);

CREATE INDEX idx_ad_sightings_ad_created
    ON ad_sightings(ad_id, created_at DESC);
