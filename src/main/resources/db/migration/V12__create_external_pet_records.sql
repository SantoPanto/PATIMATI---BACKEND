-- V10: external_pet_records — AI tarafından analiz edilmiş Instagram
-- hayvan adayı. Karşılığı: Faz 2 revize blueprint §2.
--
-- ai_embeddings/ai_labels/ai_status sütunları BİLİNÇLİ olarak ads
-- tablosundaki karşılıklarıyla aynı şekle sahiptir (ai_status hatta aynı
-- enum'u paylaşır) — candidate gathering katmanının iki tabloyu simetrik
-- okuyabilmesi için.

CREATE TABLE IF NOT EXISTS external_pet_records (
    id                       BIGSERIAL PRIMARY KEY,
    external_source_post_id  BIGINT NOT NULL REFERENCES external_source_posts(id),
    pet_index                SMALLINT NOT NULL DEFAULT 0,
    category                 VARCHAR(16) NOT NULL DEFAULT 'UNCERTAIN',
    category_confidence      REAL,
    species                  VARCHAR(16),
    breed                    VARCHAR(64),
    breed_confidence         REAL,
    colors                   JSONB,
    gender                   VARCHAR(16),
    age_text                 VARCHAR(64),
    pet_name                 VARCHAR(64),
    location_text            VARCHAR(256),
    location                 geometry(Point, 4326),
    location_confidence      REAL,
    event_date               DATE,
    event_date_estimated     BOOLEAN NOT NULL DEFAULT FALSE,
    distinguishing_features  TEXT,
    needs_review             BOOLEAN NOT NULL DEFAULT FALSE,
    ai_embeddings            JSONB,
    ai_labels                JSONB,
    ai_is_pet                BOOLEAN,
    ai_model_version         VARCHAR(64),
    ai_status                VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    ai_processed_at          TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_pet_record_index UNIQUE (external_source_post_id, pet_index)
    );

-- Konum bilinmeyen kayıtları elemek yerine (blueprint §21/§42) yalnızca
-- konumu OLAN kayıtları indeksliyoruz — PostGIS aday sorgusu bunu kullanır,
-- konumsuz kayıtlar ayrı, sınırlı bir fallback sorgusuyla bulunur.
CREATE INDEX IF NOT EXISTS idx_external_pet_records_location
    ON external_pet_records USING GIST (location)
    WHERE location IS NOT NULL;

-- Aday süzme sorgusunun taradığı alanlar — ads.idx_ads_ai_candidates ile
-- aynı gerekçe: yalnızca aday OLABİLECEK (analizi bitmiş) satırlar indekslenir.
CREATE INDEX IF NOT EXISTS idx_external_pet_records_candidates
    ON external_pet_records (category, ai_status)
    WHERE ai_status = 'DONE' AND ai_embeddings IS NOT NULL;
