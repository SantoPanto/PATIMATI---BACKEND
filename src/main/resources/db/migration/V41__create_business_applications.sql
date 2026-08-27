-- Kurum sahibi olma başvuruları: kullanıcı Hizmetler sayfasından başvurur,
-- admin onaylar/reddeder. Onaylanınca vet_clinics/petshops/shelters'a
-- karşılık gelen satır BusinessApplicationService tarafından servis
-- katmanında oluşturulur -- burada trigger/otomatik kopyalama YOK.
CREATE TABLE business_applications (
    id BIGSERIAL PRIMARY KEY,
    applicant_user_id BIGINT NOT NULL REFERENCES users(uid),
    business_type VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    phone VARCHAR(30) NOT NULL,
    working_hours VARCHAR(300),
    photo_reference VARCHAR(500),
    location geometry(Point, 4326),
    status VARCHAR(20) NOT NULL DEFAULT 'BEKLEMEDE',
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    decided_at TIMESTAMPTZ,
    decided_by_admin_id BIGINT REFERENCES users(uid)
);

CREATE INDEX idx_business_applications_status ON business_applications(status);
CREATE INDEX idx_business_applications_applicant ON business_applications(applicant_user_id);

CREATE TABLE business_application_animal_types (
    business_application_id BIGINT NOT NULL REFERENCES business_applications(id) ON DELETE CASCADE,
    animal_type VARCHAR(30) NOT NULL,
    PRIMARY KEY (business_application_id, animal_type)
);
