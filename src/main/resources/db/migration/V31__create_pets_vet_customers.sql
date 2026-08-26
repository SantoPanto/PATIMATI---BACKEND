-- Kullanıcıların kendi hayvanlarını kalıcı olarak kaydettiği "Evcil
-- Hayvanlarım" kaydı -- ilan (ads) sisteminden BAĞIMSIZ.
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(uid),
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    gender VARCHAR(20),
    age_group VARCHAR(20),
    photo_reference VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pets_owner_id ON pets(owner_id);

-- requester_id/vet_id üzerinde UNIQUE: aynı çift için tek satır --
-- reddedilirse ayni satır PENDING'e geri döner (yeniden istek), ayrı bir
-- geçmiş tutulmaz (kullanıcı kararı: basit tutulacak).
CREATE TABLE vet_customer_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(uid),
    vet_id BIGINT NOT NULL REFERENCES users(uid),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_at TIMESTAMPTZ,
    UNIQUE (requester_id, vet_id)
);
CREATE INDEX idx_vet_customer_requests_vet_status ON vet_customer_requests(vet_id, status);

CREATE TABLE pet_treatment_notes (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    vet_id BIGINT NOT NULL REFERENCES users(uid),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_pet_treatment_notes_pet_id ON pet_treatment_notes(pet_id);
