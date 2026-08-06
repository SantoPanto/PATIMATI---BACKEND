-- 1. Eklentiler
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Users Tablosu
--
-- DİKKAT: birincil anahtarın adı "uid" — "id" DEĞİL.
-- Sebebi User entity'sinin alan adının uid olması (@Column verilmemiş, yani
-- Hibernate kolonun adının da uid olmasını bekliyor) ve V6'nın yabancı
-- anahtarını buraya bağlaması. Daha önce burada "id" yazıyordu ve boş bir
-- veritabanında V6 şu hatayla duruyordu:
--   ERROR: column "uid" referenced in foreign key constraint does not exist
--
-- Kolonlar entity ile birebir tutulur; eksik bırakılan bir kolon ancak
-- ddl-auto=update sayesinde sessizce oluşuyordu, yani şema gerçekte
-- migration'ların anlattığından farklıydı.
CREATE TABLE IF NOT EXISTS users (
    uid        BIGSERIAL PRIMARY KEY,
    google_id  VARCHAR(255) UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    password   VARCHAR(255),
    phone      VARCHAR(15)  UNIQUE,
    -- Role: GUEST | USER | ADMIN (EnumType.STRING, uzunluk verilmediği için 255)
    role       VARCHAR(255) NOT NULL,
    fcm_token  VARCHAR(255),
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
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