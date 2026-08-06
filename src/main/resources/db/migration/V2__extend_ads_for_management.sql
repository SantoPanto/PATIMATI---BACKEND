ALTER TABLE ads
    ADD COLUMN IF NOT EXISTS species VARCHAR(20);

UPDATE ads
SET species = 'OTHER'
WHERE species IS NULL;

ALTER TABLE ads
    ALTER COLUMN species SET NOT NULL;

ALTER TABLE ads
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ads
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS ad_photo_urls
(
    ad_id       BIGINT        NOT NULL,
    photo_url   VARCHAR(2048) NOT NULL,
    photo_order INTEGER       NOT NULL,
    CONSTRAINT pk_ad_photo_urls PRIMARY KEY (ad_id, photo_order),
    CONSTRAINT fk_ad_photo_urls_ad
    FOREIGN KEY (ad_id) REFERENCES ads (id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_ad_photo_urls_ad_id
    ON ad_photo_urls (ad_id);