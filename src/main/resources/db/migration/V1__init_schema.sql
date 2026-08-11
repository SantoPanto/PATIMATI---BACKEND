-- 1. Eklentiler
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Users Tablosu

CREATE TABLE IF NOT EXISTS users (
    uid        BIGSERIAL PRIMARY KEY,
    google_id  VARCHAR(255) UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    password   VARCHAR(255),
    phone      VARCHAR(15)  UNIQUE,
    role       VARCHAR(255) NOT NULL,
    fcm_token  VARCHAR(255),
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    location   geometry(Point, 4326)
    );

-- 3. Ads Tablosu
CREATE TABLE IF NOT EXISTS ads (
                                   id BIGSERIAL PRIMARY KEY,
                                   title VARCHAR(255),
    description TEXT,
    ad_type VARCHAR(50),
    location geometry(Point, 4326),
    user_id BIGINT,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_ads_user FOREIGN KEY (user_id) REFERENCES users(uid)
    );

-- 4. İndeksler
CREATE INDEX IF NOT EXISTS idx_ads_location ON ads USING GIST (location);