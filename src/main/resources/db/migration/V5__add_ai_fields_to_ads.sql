-- V4: AI analiz sonuçlarının yazılacağı alanlar.
-- Karşılığı: PATIMATI-AI deposundaki docs/entegrasyon-sozlesmesi.md §6.
--
-- Not: ddl-auto=validate olduğu için şema değişikliği YALNIZCA buradan yapılır;
-- entity'ye alan ekleyip bu dosyayı yazmazsak uygulama hiç açılmaz.

-- Her fotoğraf için bir vektör: [{"photoUrl": "...", "embedding": [768 float]}, ...]
-- Neden jsonb ve neden liste: tek fotoğrafla eşleşme oranı gerçek veride %24'te
-- kaldı, çoklu fotoğraf zorunlu hâle geldi (ölçüm raporu §4). Vektörü fotoğraf
-- adresiyle birlikte saklamak, fotoğraf sırası değişse bile eşlemeyi korur.
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_embeddings jsonb;

-- Eşleştirme skorunun %30'unu oluşturan etiketler: ["cat","tabby","brown"]
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_labels jsonb;

-- AI'ın tür tahmini. Kullanıcının beyanından (ads.species) AYRI tutulur:
-- sözleşme §7 kural 2 gereği beyan önceliklidir, AI tahmini onu ezmez.
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_species VARCHAR(16);

-- Cins bilgi amaçlıdır, FİLTRE DEĞİLDİR (sözleşme §7 kural 1): Türkiye'de kayıp
-- hayvanların çoğu melez, yanlış cins tahmini gerçek eşleşmeyi elerdi.
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_breed VARCHAR(64);
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_breed_confidence REAL;

-- Vektörü hangi model üretti. Model değişince eski vektörler yenileriyle
-- KIYASLANAMAZ; bu alan olmadan hangilerinin bayat olduğu anlaşılamaz.
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_model_version VARCHAR(64);

-- PENDING | DONE | FAILED. Varsayılan PENDING: ilan kaydedildiği anda AI henüz
-- çalışmamıştır, kullanıcı beklemez (asenkron akış, sözleşme §1).
ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_status VARCHAR(16) NOT NULL DEFAULT 'PENDING';

ALTER TABLE ads ADD COLUMN IF NOT EXISTS ai_processed_at TIMESTAMPTZ;

-- Aday süzme sorgusunun (sözleşme §5) taradığı alanlar.
-- Kısmi indeks: yalnızca aday OLABİLECEK satırlar indekslenir — analizi bitmiş,
-- vektörü olan ve aktif ilanlar. Tüm tabloyu indekslemekten belirgin ölçüde küçük.
CREATE INDEX IF NOT EXISTS idx_ads_ai_candidates
    ON ads (ad_type, created_at)
    WHERE active = TRUE AND ai_status = 'DONE' AND ai_embeddings IS NOT NULL;

-- Analizi bekleyen/başarısız olan ilanları bulmak için (yeniden analiz, izleme).
CREATE INDEX IF NOT EXISTS idx_ads_ai_status
    ON ads (ai_status)
    WHERE ai_status <> 'DONE';
