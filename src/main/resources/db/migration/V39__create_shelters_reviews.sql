CREATE TABLE shelters (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(uid),
    name            VARCHAR(200) NOT NULL,
    address         VARCHAR(500) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    district        VARCHAR(100),
    phone           VARCHAR(30) NOT NULL,
    working_hours   VARCHAR(300),
    photo_reference VARCHAR(500),
    location        geometry(Point, 4326),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_shelters_location ON shelters USING GIST (location);

CREATE TABLE shelter_reviews (
    id          BIGSERIAL PRIMARY KEY,
    shelter_id  BIGINT NOT NULL REFERENCES shelters(id) ON DELETE CASCADE,
    author_id   BIGINT NOT NULL REFERENCES users(uid),
    -- INTEGER, SMALLINT DEĞİL: V34/V35 dersi -- Hibernate schema-validation
    -- Java tipini (Integer) SQL tipine göre açılışta doğruluyor,
    -- columnDefinition override'ı yalnızca DDL üretimini etkiliyor.
    rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (shelter_id, author_id)
);
CREATE INDEX idx_shelter_reviews_shelter ON shelter_reviews(shelter_id);
