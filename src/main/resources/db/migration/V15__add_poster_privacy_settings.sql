-- V15: İlan afiş (poster) gizlilik ve KVKK ayarlarını ekler

ALTER TABLE ads
    ADD COLUMN is_poster_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN show_email_on_poster BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN show_phone_on_poster BOOLEAN NOT NULL DEFAULT FALSE;
