-- 1. PostGIS eklentisini aktifleştir
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Tabloyu oluştur (Arkadaşının 2. maddesi: geography kullanımı)
CREATE TABLE locations (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255),
    -- geometry yerine geography kullanıyoruz
                           coordinate geography(Point, 4326)
);

-- (Varsa users vb. diğer tablolarınızın CREATE kodlarını da buraya eklemelisiniz)

-- 3. GIST İndeksini oluştur
CREATE INDEX idx_locations_coordinate ON locations USING GIST (coordinate);