-- V9: Instagram ingestion — ham kaynak tabloları.
-- Karşılığı: Faz 2 revize blueprint §2.
--
-- Bu üç tablo yalnızca "ne toplandı" bilgisini tutar; AI analizi ve
-- eşleştirme sonraki migration'lardaki tablolarda yaşar. Instagram kaydı
-- HİÇBİR ZAMAN normal PatiMati ilanı (ads) DEĞİLDİR — bu yüzden tamamen
-- ayrı bir tablo ailesi.

-- Kanonik gönderi kimliği. Aynı gönderi POST_TAG + CAPTION_MENTION +
-- COMMENT_MENTION üçüyle birden tespit edilse bile bu tabloda TEK satır
-- oluşur (kanonik kimlik, blueprint §18); her tespit external_source_events'e
-- ayrı satır olarak düşer.
CREATE TABLE IF NOT EXISTS external_source_posts (
    id                BIGSERIAL PRIMARY KEY,
    source            VARCHAR(16)  NOT NULL,
    source_post_id    VARCHAR(64)  NOT NULL,
    canonical_url     VARCHAR(512) NOT NULL,
    author_username   VARCHAR(128),
    caption           TEXT,
    published_at      TIMESTAMPTZ,
    detected_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    location_text     VARCHAR(256),
    media_count       SMALLINT NOT NULL DEFAULT 0,
    processing_status VARCHAR(16) NOT NULL DEFAULT 'DISCOVERED',
    source_unavailable BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason    VARCHAR(512),
    analysis_version  VARCHAR(64),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_source_post UNIQUE (source, source_post_id)
    );

-- Bir gönderiyi bize işaret eden tek bir tetikleyici.
CREATE TABLE IF NOT EXISTS external_source_events (
    id                     BIGSERIAL PRIMARY KEY,
    external_source_post_id BIGINT NOT NULL REFERENCES external_source_posts(id),
    trigger_type           VARCHAR(16) NOT NULL,
    external_trigger_id    VARCHAR(64),
    triggering_comment     TEXT,
    detected_at            TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- POST_TAG ve CAPTION_MENTION gönderi başına en fazla bir kez anlamlıdır.
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_singleton
    ON external_source_events (external_source_post_id, trigger_type)
    WHERE trigger_type IN ('POST_TAG', 'CAPTION_MENTION');

-- COMMENT_MENTION birden fazla olabilir ama aynı Instagram yorumu iki kez
-- ayrı event üretmemeli.
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_comment
    ON external_source_events (external_source_post_id, external_trigger_id)
    WHERE trigger_type = 'COMMENT_MENTION' AND external_trigger_id IS NOT NULL;

-- Carousel fotoğrafları. Sıra (ordinal) korunur; storage_key doldurulana
-- kadar (PENDING) fotoğraf henüz bizim S3 kovamıza taşınmamış demektir —
-- Collector'ın gönderdiği orijinal Instagram adresine (source_reference)
-- ASLA güvenilmez, yalnızca denetim amaçlı saklanır (blueprint §16/§20).
CREATE TABLE IF NOT EXISTS external_source_media (
    id                      BIGSERIAL PRIMARY KEY,
    external_source_post_id BIGINT NOT NULL REFERENCES external_source_posts(id),
    ordinal                 SMALLINT NOT NULL,
    media_type              VARCHAR(16) NOT NULL DEFAULT 'IMAGE',
    source_reference        VARCHAR(512),
    storage_key             VARCHAR(512),
    content_sha256          CHAR(64),
    processing_state        VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_source_media_ordinal UNIQUE (external_source_post_id, ordinal)
    );

CREATE INDEX IF NOT EXISTS idx_external_source_posts_status
    ON external_source_posts (processing_status);
