-- V21: potential_matches + potential_match_recipients.
-- Karşılığı: Faz 2 revize blueprint §3 (düzeltilmiş iki-tablo tasarımı).
--
-- potential_matches yalnızca "hangi çift eşleşti" kimliğidir.
-- potential_match_recipients "kime bildirilecek ve o kullanıcının kararı
-- ne" bilgisidir — native↔native bir eşleşmede İKİ bağımsız satır olur,
-- native↔external'da BİR satır olur. Bu ayrım, taslak sürümdeki
-- "source_ad_id hem kimlik hem bildirim hedefi" karışıklığını çözer.

CREATE TABLE IF NOT EXISTS potential_matches (
    id                   BIGSERIAL PRIMARY KEY,
    candidate_kind       VARCHAR(16) NOT NULL,
    ad_a_id              BIGINT NOT NULL REFERENCES ads(id),
    ad_b_id              BIGINT REFERENCES ads(id),
    external_record_id   BIGINT REFERENCES external_pet_records(id),
    visual_score         REAL NOT NULL,
    label_score          REAL NOT NULL,
    location_score       REAL NOT NULL,
    final_score          REAL NOT NULL,
    matching_version     VARCHAR(64) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_potential_match_candidate_kind CHECK (
        (candidate_kind = 'AD'       AND ad_b_id IS NOT NULL AND external_record_id IS NULL AND ad_a_id <> ad_b_id) OR
        (candidate_kind = 'EXTERNAL' AND ad_b_id IS NULL     AND external_record_id IS NOT NULL)
        )
    );

-- Aynı AD-AD çifti, hangi ilanın analizi eşleşmeyi önce bulduğuna
-- bakılmaksızın DAİMA aynı satıra düşer — LEAST/GREATEST ile kanonik sıra
-- veritabanı seviyesinde zorlanır (uygulama katmanındaki kanonik sıralama
-- ikinci bir güvenlik katmanıdır, tek başına yeterli kabul edilmez).
CREATE UNIQUE INDEX IF NOT EXISTS uq_potential_match_ad
    ON potential_matches (LEAST(ad_a_id, ad_b_id), GREATEST(ad_a_id, ad_b_id))
    WHERE candidate_kind = 'AD';

CREATE UNIQUE INDEX IF NOT EXISTS uq_potential_match_external
    ON potential_matches (ad_a_id, external_record_id)
    WHERE candidate_kind = 'EXTERNAL';

-- "Kime bildirilecek" ve dayanıklı bildirim teslimat niyeti (blueprint §4).
-- Ayrı bir outbox tablosuna gerek yok: bu satırın kendisi hem niyet hem
-- durum kaydıdır.
CREATE TABLE IF NOT EXISTS potential_match_recipients (
    id                  BIGSERIAL PRIMARY KEY,
    potential_match_id  BIGINT NOT NULL REFERENCES potential_matches(id),
    recipient_user_id   BIGINT NOT NULL REFERENCES users(uid),
    role                VARCHAR(16) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    send_attempts       SMALLINT NOT NULL DEFAULT 0,
    notified_at         TIMESTAMPTZ,
    viewed_at           TIMESTAMPTZ,
    decided_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Ne column nullable: düz UNIQUE burada güvenli ve yeterlidir
    -- (partial-indeks tuzağı yalnızca nullable sütunlarda oluşur, bkz. yukarısı).
    CONSTRAINT uq_potential_match_recipient UNIQUE (potential_match_id, recipient_user_id)
    );

-- Bildirim yeniden-deneme süpürücüsünün taradığı alan.
CREATE INDEX IF NOT EXISTS idx_potential_match_recipients_pending
    ON potential_match_recipients (status, created_at)
    WHERE status = 'PENDING';
