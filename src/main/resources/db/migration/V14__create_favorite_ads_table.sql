CREATE TABLE IF NOT EXISTS favorite_ads (
    id BIGSERIAL PRIMARY KEY,
    user_uid BIGINT NOT NULL,
    ad_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_favorite_ads_user_ad UNIQUE (user_uid, ad_id),
    CONSTRAINT fk_favorite_ads_user FOREIGN KEY (user_uid) REFERENCES users (uid) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_ads_ad FOREIGN KEY (ad_id) REFERENCES ads (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_favorite_ads_user_uid ON favorite_ads (user_uid);
CREATE INDEX IF NOT EXISTS idx_favorite_ads_ad_id ON favorite_ads (ad_id);
