-- 1. adoption_complaints tablosunu oluştur
CREATE TABLE IF NOT EXISTS adoption_complaints (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    ad_id BIGINT NOT NULL,
    reason VARCHAR(30) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BEKLEMEDE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_adoption_complaints_ad FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_adoption_complaints_reporter FOREIGN KEY (reporter_id) REFERENCES users(uid) ON DELETE CASCADE
);

-- 2. Indeksler (Mükerrerlik kontrolleri ve sorgu performansı için)
CREATE INDEX IF NOT EXISTS idx_adoption_complaint_reporter_ad_status ON adoption_complaints (reporter_id, ad_id, status);
CREATE INDEX IF NOT EXISTS idx_adoption_complaint_status ON adoption_complaints (status);

-- 3. AdMatch tablosunda Hibernate validate doğrulaması için eksik olan indeksi ekle
CREATE INDEX IF NOT EXISTS idx_ad_match_passed_threshold ON ad_match (passed_threshold);
