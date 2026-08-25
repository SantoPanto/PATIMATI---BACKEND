-- Gerçek Instagram CDN URL'leri (imzalı sorgu parametreleriyle) 512 karakteri
-- rahatlıkla aşabiliyor -- varchar(512) canlı ortamda her external medya
-- eklemesinde "value too long for type character varying(512)" hatasıyla
-- kuyruğu tıkıyordu (mesaj ACK edilmediği için sonsuz yeniden teslim).
ALTER TABLE external_source_media
    ALTER COLUMN source_reference TYPE text;
