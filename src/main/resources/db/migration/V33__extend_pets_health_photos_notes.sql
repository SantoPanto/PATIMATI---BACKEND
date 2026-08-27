-- "Evcil Hayvanlarım" zenginleştirmesi (26.08): yapılandırılmış sağlık
-- alanları, çoklu fotoğraf, aşı/kilo takibi, sahibin de not girebilmesi.
-- Hepsi ADDITIVE — mevcut satırlar/uçlar davranış değiştirmez.

ALTER TABLE pets
    ADD COLUMN birth_date DATE,
    ADD COLUMN sterilized BOOLEAN,
    ADD COLUMN microchip_number VARCHAR(50),
    ADD COLUMN chronic_conditions VARCHAR(1000),
    ADD COLUMN allergies VARCHAR(1000),
    ADD COLUMN ai_report TEXT,
    ADD COLUMN ai_report_at TIMESTAMPTZ;

-- Sahip de tedavi notu girebilsin diye vet_id artık zorunlu değil.
-- author_type mevcut satırları geriye dönük VET işaretler (dolu tabloya
-- NOT NULL sütun DEFAULT'suz eklenemez — bkz. OKU-ONCE.md #3).
ALTER TABLE pet_treatment_notes
    ALTER COLUMN vet_id DROP NOT NULL,
    ADD COLUMN author_type VARCHAR(20) NOT NULL DEFAULT 'VET';

CREATE TABLE pet_photos (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    photo_reference VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pet_photos_pet_id ON pet_photos(pet_id);

CREATE TABLE pet_vaccinations (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    recorded_by BIGINT REFERENCES users(uid),
    vaccine_name VARCHAR(200) NOT NULL,
    administered_date DATE NOT NULL,
    next_due_date DATE,
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pet_vaccinations_pet_id ON pet_vaccinations(pet_id);

CREATE TABLE pet_weight_logs (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    recorded_by BIGINT REFERENCES users(uid),
    weight_kg NUMERIC(5,2) NOT NULL,
    recorded_at DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pet_weight_logs_pet_id ON pet_weight_logs(pet_id);
