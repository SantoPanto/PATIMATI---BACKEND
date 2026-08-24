CREATE TABLE IF NOT EXISTS ad_match (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_ad_id BIGINT NOT NULL,
    matched_ad_id BIGINT NOT NULL,
    total_score DOUBLE PRECISION NOT NULL,
    visual_score DOUBLE PRECISION,
    tag_score DOUBLE PRECISION,
    location_score DOUBLE PRECISION,
    threshold_at_time DOUBLE PRECISION NOT NULL,
    passed_threshold BOOLEAN NOT NULL,
    block_reason VARCHAR(1000),
    matched_photo_pair VARCHAR(500),
    notification_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_ad_match_user_source_matched UNIQUE (user_id, source_ad_id, matched_ad_id),
    CONSTRAINT fk_ad_match_user FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE CASCADE,
    CONSTRAINT fk_ad_match_source_ad FOREIGN KEY (source_ad_id) REFERENCES ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_ad_match_matched_ad FOREIGN KEY (matched_ad_id) REFERENCES ads(id) ON DELETE CASCADE
);

CREATE INDEX idx_ad_match_user_score ON ad_match(user_id, total_score DESC);
CREATE INDEX idx_ad_match_source ON ad_match(source_ad_id);
CREATE INDEX idx_ad_match_matched ON ad_match(matched_ad_id);