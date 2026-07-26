-- 1. Eklentiler
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Users Tablosu
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255),
    email VARCHAR(255) UNIQUE
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
    CONSTRAINT fk_ads_user FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- 4. İndeksler
CREATE INDEX IF NOT EXISTS idx_ads_location ON ads USING GIST (location);