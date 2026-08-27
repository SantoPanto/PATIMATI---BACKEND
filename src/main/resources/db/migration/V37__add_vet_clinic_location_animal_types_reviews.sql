ALTER TABLE vet_clinics ADD COLUMN location geometry(Point, 4326);
CREATE INDEX idx_vet_clinics_location ON vet_clinics USING GIST (location);

CREATE TABLE vet_clinic_animal_types (
    vet_clinic_id BIGINT NOT NULL REFERENCES vet_clinics(id) ON DELETE CASCADE,
    animal_type   VARCHAR(30) NOT NULL,
    PRIMARY KEY (vet_clinic_id, animal_type)
);

CREATE TABLE vet_clinic_reviews (
    id            BIGSERIAL PRIMARY KEY,
    vet_clinic_id BIGINT NOT NULL REFERENCES vet_clinics(id) ON DELETE CASCADE,
    author_id     BIGINT NOT NULL REFERENCES users(uid),
    -- INTEGER (SMALLINT değil): Hibernate şema doğrulaması entity alanının
    -- Java tipine (Integer) göre INTEGER bekliyor; SMALLINT ile canlı
    -- açılışta "wrong column type" hatası alındı, düzeltildi.
    rating        INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       VARCHAR(1000),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (vet_clinic_id, author_id)
);
CREATE INDEX idx_vet_clinic_reviews_clinic ON vet_clinic_reviews(vet_clinic_id);
