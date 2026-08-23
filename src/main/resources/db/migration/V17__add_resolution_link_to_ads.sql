-- Kayıp ilan "bulundu" diye kapanınca HANGİ ilanla eşleştiğini kaydeder.
--
-- NEDEN: bugün kapanışta yalnız resolution_status = 'FOUND' yazılıyor.
-- İsteğe gelen finderId ise ödül puanı verilip ATILIYOR, hiçbir yerde
-- saklanmıyor. Sonuç: sistemde "şu kayıp ilan şu bulundu ilanıyla eşleşti"
-- bilgisi HİÇ YOK.
--
-- Bunun somut bedeli ölçüldü: eşleşme skorundaki konum kanalının ağırlığı
-- (0.15) tartışılıyor ama karar verilemiyor, çünkü "gerçekte eşleşen bir
-- çiftin arası kaç km" sorusunun cevabı hiçbir yerde yok. Elimizdeki
-- ölçümde pozitif ve negatif çiftlere aynı mesafe verilmek zorunda kalındı,
-- o yüzden konumun AYIRT ETME değeri ölçülemedi.
--
-- Bu iki sütun dolmaya başladığında o soru ölçümle cevaplanabilir hâle
-- gelir: eşleşen çiftlerin mesafe dağılımı ile rastgele ilan çiftlerinin
-- dağılımı karşılaştırılır.
--
-- NULL BIRAKILABİLİR, bilerek: geçmiş satırlarda bu bilgi yok ve bundan
-- sonra da kullanıcı bulan ilanı seçmeden kapatabilir. NOT NULL yapmak
-- dolu tabloda migration'ı düşürürdü (bkz. OKU-ONCE.md §3).

ALTER TABLE ads
    ADD COLUMN IF NOT EXISTS resolved_by_ad_id BIGINT,
    ADD COLUMN IF NOT EXISTS finder_user_id BIGINT;

-- İlan silinirse bağ kopar ama kayıp ilan ayakta kalır: SET NULL.
-- CASCADE olsaydı bulundu ilanının silinmesi kayıp ilanı da silerdi.
ALTER TABLE ads
    ADD CONSTRAINT fk_ads_resolved_by_ad
        FOREIGN KEY (resolved_by_ad_id) REFERENCES ads (id) ON DELETE SET NULL;

ALTER TABLE ads
    ADD CONSTRAINT fk_ads_finder_user
        FOREIGN KEY (finder_user_id) REFERENCES users (uid) ON DELETE SET NULL;

-- Bir ilan kendisiyle eşleşemez. Uygulama katmanı da denetliyor; kısıt,
-- veriye başka bir yoldan (elle SQL, toplu göç) girilmesine karşı.
ALTER TABLE ads
    ADD CONSTRAINT ck_ads_resolved_by_not_self
        CHECK (resolved_by_ad_id IS NULL OR resolved_by_ad_id <> id);

-- Ölçüm sorgusu "çözülmüş ilanları ve eşleştikleri ilanı" tarayacak.
CREATE INDEX IF NOT EXISTS idx_ads_resolved_by ON ads (resolved_by_ad_id)
    WHERE resolved_by_ad_id IS NOT NULL;
