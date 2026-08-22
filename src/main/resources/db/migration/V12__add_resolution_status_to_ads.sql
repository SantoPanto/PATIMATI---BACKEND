-- Manuel kapatma ile başarılı ilan sonuçlarını birbirinden ayırır.
-- Mevcut ilanlarda sonuç bilgisi olmadığından NONE ile başlatılır.
ALTER TABLE ads
    ADD COLUMN IF NOT EXISTS resolution_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
