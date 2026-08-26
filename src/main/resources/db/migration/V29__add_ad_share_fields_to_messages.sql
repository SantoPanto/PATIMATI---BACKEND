/*
 * Messages tablosuna ilan paylaşımı desteği için gerekli alanları ekler.
 *
 * message_type: Mesajın türü (TEXT veya AD_SHARE). Varsayılan: TEXT
 * shared_ad_id: Paylaşılan ilanın ID referansı.
 */
ALTER TABLE messages
    ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    ADD COLUMN shared_ad_id BIGINT NULL;

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_shared_ad
        FOREIGN KEY (shared_ad_id)
            REFERENCES ads (id)
            ON DELETE SET NULL;
