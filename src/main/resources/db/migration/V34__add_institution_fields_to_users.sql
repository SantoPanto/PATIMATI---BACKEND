-- Belediye modülü — B parçası: kurum (belediye) hesabı.
--
-- Kurum hesabı ayrı bir tablo DEĞİL, users satırının kendisidir: role sütunu
-- INSTITUTION olur ve aşağıdaki üç alan dolar. Ayrı tablo açmamanın sebebi,
-- kurumun da giriş yapan, bildirim alan, mesajlaşan bir kullanıcı olması —
-- ikinci bir kimlik tablosu tüm bu yolları çatallaştırırdı.
--
-- ÜÇÜ DE NULL OLABİLİR: users bugün dolu (canlıda gerçek kullanıcılar var) ve
-- bu satırların hiçbiri kuruma ait değil. NOT NULL + DEFAULT vermek her normal
-- kullanıcıya anlamsız bir kurum adı yazardı. Kurum olmayan satırda üçü de
-- NULL kalır; doluluk yalnızca role = 'INSTITUTION' satırlarında beklenir.
--
-- users.role üzerinde CHECK KISITI YOK (V1'de `role varchar(255) NOT NULL`,
-- sonraki hiçbir göç kısıt eklememiş — 26.08'de tüm göç dosyaları tarandı).
-- Bu yüzden yeni enum değeri için DDL gerekmiyor; Java tarafındaki
-- User.Role yeterli.
--
-- İLÇE DEĞERİ NEREDEN GELİYOR: panel sorguları ads.district ile kıyaslayacak.
-- ads.district'i ReverseGeocodingService dolduruyor (Nominatim, accept-language=tr,
-- county/town/district/suburb) — yani "Nilüfer" gibi Türkçe, OSM yazımıyla.
-- Kuruma ilçe atanırken AYNI yazım kullanılmalı; kıyas yine de büyük/küçük
-- harf duyarsız yapılmalı (bkz. MunicipalityScopeService).
ALTER TABLE users ADD COLUMN institution_name varchar(150);
ALTER TABLE users ADD COLUMN institution_city varchar(100);
ALTER TABLE users ADD COLUMN institution_district varchar(100);

-- Kurum hesabı sayısı çok az (ilçe başına bir), ama panelin her isteği
-- oturum sahibinin ilçesini çözmek için bu satırı okuyacak. Kısmi indeks
-- yalnız kurum satırlarını kapsar: normal kullanıcı satırları indekse hiç
-- girmez, tablo büyüdükçe indeks büyümez.
CREATE INDEX IF NOT EXISTS idx_users_institution_district
    ON users (institution_district)
    WHERE institution_district IS NOT NULL;
