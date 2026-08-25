-- İlanların PatiMati'nin Instagram hesabında paylaşılması özelliği.
--
-- instagram_share_consent: ilan sahibinin verdiği ayrı rıza -- afiş
-- (poster) rızasıyla (is_poster_allowed) AYNI KAPSAMDA DEĞİL, o birebir bir
-- kişiye verilen PDF içindir, bu herkese açık bir sosyal medya gönderisi
-- içindir (kullanıcı kararı). Varsayılan false: sessizce izin verilmiş
-- sayılmaz, mevcut ilanlar da etkilenmez.
ALTER TABLE ads ADD COLUMN instagram_share_consent BOOLEAN NOT NULL DEFAULT FALSE;

-- İlan başına en fazla bir satır (queueForReview bir kez ekler, sonraki
-- yayınla/atla/tekrar-dene işlemleri AYNI satırı günceller -- bkz.
-- AdInstagramPublication javadoc'u).
CREATE TABLE ad_instagram_publications (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    suggested_caption TEXT,
    final_caption TEXT,
    ig_media_id VARCHAR(64),
    ig_permalink VARCHAR(512),
    failure_reason VARCHAR(1000),
    published_by_admin_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ad_instagram_publications_ad
        FOREIGN KEY (ad_id)
        REFERENCES ads(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ad_instagram_publications_status
    ON ad_instagram_publications(status);
