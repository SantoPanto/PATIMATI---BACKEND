CREATE TABLE petshop_reviews (
    id          BIGSERIAL PRIMARY KEY,
    petshop_id  BIGINT NOT NULL REFERENCES petshops(id) ON DELETE CASCADE,
    author_id   BIGINT NOT NULL REFERENCES users(uid),
    -- INTEGER, SMALLINT DEĞİL: V34/V35/V36 dersi -- Hibernate schema-validation
    -- Java tipini (Integer) SQL tipine göre açılışta doğruluyor,
    -- columnDefinition override'ı yalnızca DDL üretimini etkiliyor.
    rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (petshop_id, author_id)
);
CREATE INDEX idx_petshop_reviews_petshop ON petshop_reviews(petshop_id);
