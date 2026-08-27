-- Veteriner klinik bilgi kartı: kullanıcı başına tek satır (user_id UNIQUE),
-- AlertSubscription (V21) ile AYNI desen. Burada REQUIRES_NEW transaction
-- sınırı söz konusu değil (Instagram kuyruğundaki FK kaçınma gerekçesi
-- geçerli değil) -- klinik satırı her zaman ilgili User satırı zaten
-- commit edilmiş haldeyken, normal istek transaction'ı içinde oluşturulur.
-- Bu yüzden gerçek bir FK kullanılıyor.
CREATE TABLE vet_clinics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(uid),
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    phone VARCHAR(30) NOT NULL,
    working_hours VARCHAR(300),
    photo_reference VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vet_clinics_city ON vet_clinics(city);
