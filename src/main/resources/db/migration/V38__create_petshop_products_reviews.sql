CREATE TABLE petshops (
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
CREATE INDEX idx_petshops_location ON petshops USING GIST (location);

CREATE TABLE petshop_products (
    id              BIGSERIAL PRIMARY KEY,
    petshop_id      BIGINT NOT NULL REFERENCES petshops(id) ON DELETE CASCADE,
    name            VARCHAR(200) NOT NULL,
    description     VARCHAR(1000),
    price           NUMERIC(10,2) NOT NULL CHECK (price > 0),
    photo_reference VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_petshop_products_petshop ON petshop_products(petshop_id);

CREATE TABLE petshop_product_reviews (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES petshop_products(id) ON DELETE CASCADE,
    author_id   BIGINT NOT NULL REFERENCES users(uid),
    -- INTEGER, SMALLINT DEĞİL: V34'te Integer alanı SMALLINT'e karşı
    -- Hibernate schema-validation'da "wrong column type" ile açılışta
    -- patlamıştı (columnDefinition override validasyonu etkilemiyor,
    -- yalnızca DDL üretimini etkiliyor). Aynı hata tekrarlanmasın diye
    -- baştan INTEGER.
    rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (product_id, author_id)
);
CREATE INDEX idx_petshop_product_reviews_product ON petshop_product_reviews(product_id);
