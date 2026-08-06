-- 1. Varsa eski birleşik complaints tablosunu kaldır
DROP TABLE IF EXISTS complaints CASCADE;

-- 2. ad_complaints tablosunu oluştur (reported_user_id KESİNLİKLE İÇERMEZ)
CREATE TABLE IF NOT EXISTS ad_complaints (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    ad_id BIGINT NOT NULL,
    reason VARCHAR(30) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BEKLEMEDE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ad_complaints_ad FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_ad_complaints_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. user_complaints tablosunu oluştur
CREATE TABLE IF NOT EXISTS user_complaints (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    reason VARCHAR(30) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BEKLEMEDE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_complaints_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_complaints_reported_user FOREIGN KEY (reported_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Indeksler (Mükerrerlik kontrolleri ve performans için)
CREATE INDEX IF NOT EXISTS idx_ad_complaint_reporter_ad_status ON ad_complaints (reporter_id, ad_id, status);
CREATE INDEX IF NOT EXISTS idx_ad_complaint_status ON ad_complaints (status);

CREATE INDEX IF NOT EXISTS idx_user_complaint_reporter_user_status ON user_complaints (reporter_id, reported_user_id, status);
CREATE INDEX IF NOT EXISTS idx_user_complaint_status ON user_complaints (status);
