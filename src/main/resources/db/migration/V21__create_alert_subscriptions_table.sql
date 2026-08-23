-- Konum tabanlı uyarı aboneliği: "çevremde kayıp ilanı çıkınca bildirim al."
--
-- Bugüne kadar yakındaki-ilan bildirimi aboneliksizdi: AdService, HER yeni
-- ilanda (türü ne olursa olsun) konumu dolu TÜM kullanıcılara sabit 5 km
-- yarıçapla bildirim gönderiyordu — kapatma imkânı ve kullanıcı seçimi yoktu.
-- Bu tablo o mekanizmanın yerine geçer: kullanıcı ayarlardan kendi merkez
-- konumunu ve yarıçapını seçer, bildirim yalnız isteyene gider.
--
-- Yarıçap METRE cinsinden saklanır çünkü sorgu tarafında ST_DWithin
-- geography üzerinde metreyle çalışır (bkz. AdRepository.findAiCandidates
-- üstündeki uyarı: geometry üstünde birim derece olur ve dünyayı kapsar).
-- Kullanıcı başına TEK abonelik (UNIQUE): ayarlar ekranı tek bölge sunar;
-- çoklu bölge istenirse UNIQUE kaldırılarak genişletilebilir.
CREATE TABLE alert_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    location geometry(Point, 4326) NOT NULL,
    radius_meters DOUBLE PRECISION NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_alert_subscriptions_user
        FOREIGN KEY (user_id)
        REFERENCES users(uid)
        ON DELETE CASCADE,

    -- Uç zaten 1-50 km doğruluyor; buradaki CHECK, doğrulaması unutulmuş
    -- ileri bir yazma yolunun saçma yarıçap (0 ya da dünya) kaydetmesini
    -- veritabanı katında da engeller.
    CONSTRAINT chk_alert_subscriptions_radius
        CHECK (radius_meters >= 1000 AND radius_meters <= 50000)
);

-- Her ilan yayınında çalışan yarıçap sorgusunun taşıyıcısı. ads.location
-- için V1'de yapılan GIST indeksinin aynısı; users.location'da hiç
-- yapılmamıştı ve bu tablo o sorgunun yerini alıyor.
CREATE INDEX idx_alert_subscriptions_location
    ON alert_subscriptions USING GIST (location);
