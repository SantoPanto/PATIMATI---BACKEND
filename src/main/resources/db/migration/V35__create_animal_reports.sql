-- V35: Vatandas ihbarlari tablosu ve cografi indeksler
CREATE TABLE animal_reports (
                                id BIGSERIAL PRIMARY KEY,
                                reporter_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                reporter_contact VARCHAR(100) NOT NULL,
                                type VARCHAR(20) NOT NULL,
                                note TEXT,
                                photo_url VARCHAR(500),
                                location geometry(Point, 4326) NOT NULL,
                                city VARCHAR(50),
                                district VARCHAR(50),
                                status VARCHAR(20) NOT NULL DEFAULT 'YENI',
                                handled_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Mekansal (GIST) ve kompozit B-Tree indeksleri
CREATE INDEX idx_animal_reports_location ON animal_reports USING GIST (location);
CREATE INDEX idx_animal_reports_district_status_created ON animal_reports (district, status, created_at DESC);