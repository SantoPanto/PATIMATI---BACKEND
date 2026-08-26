CREATE TABLE points_of_interest (
    id            BIGSERIAL PRIMARY KEY,
    type          VARCHAR(20)  NOT NULL,
    source        VARCHAR(20)  NOT NULL,
    osm_id        BIGINT,
    name          VARCHAR(255) NOT NULL,
    location      geometry(Point, 4326) NOT NULL,
    address       VARCHAR(500),
    phone         VARCHAR(50),
    opening_hours VARCHAR(255),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_pois_source_osm_id UNIQUE (source, osm_id)
);

CREATE INDEX idx_pois_location ON points_of_interest USING GIST (location);
CREATE INDEX idx_pois_type ON points_of_interest (type);
