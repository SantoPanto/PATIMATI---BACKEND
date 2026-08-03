/*
 * Kullanıcılar arasındaki mesajları kalıcı olarak saklayan tabloyu oluşturur.
 *
 * sender_id ve recipient_id alanları users tablosunun id primary key
 * kolonuna bağlanır. Java tarafındaki User.uid alanı da @Column(name = "id")
 * ile aynı kolona eşlenmiştir.
 */
CREATE TABLE messages
(
    -- Mesajın benzersiz veritabanı kimliği
    id BIGSERIAL PRIMARY KEY,

    -- Mesajı gönderen kullanıcının users.id değeri
    sender_id BIGINT NOT NULL,

    -- Mesajı alan kullanıcının users.id değeri
    recipient_id BIGINT NOT NULL,

    -- Mesaj içeriği entity ile aynı şekilde 2000 karakterle sınırlandırılır
    content VARCHAR(2000) NOT NULL,

    -- Mesajın oluşturulduğu zaman, saat dilimi bilgisiyle birlikte saklanır
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Yeni mesajlar varsayılan olarak okunmamış kabul edilir
    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    /*
     * Boşluklardan oluşan mesajların doğrudan veritabanına
     * kaydedilmesini engeller.
     */
    CONSTRAINT chk_messages_content_not_blank
        CHECK (CHAR_LENGTH(BTRIM(content)) > 0),

    /*
     * Gönderen kullanıcı silinirse ona ait mesajları da kaldırır.
     * Bu işlem JPA cascade değil, veritabanı seviyesindeki bir kuraldır.
     */
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    /*
     * Alıcı kullanıcı silinirse ona ait mesajları da kaldırır.
     */
    CONSTRAINT fk_messages_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

/*
 * İki kullanıcı arasındaki mesajları tarih ve ID sırasıyla
 * getiren findChatHistory sorgusunu hızlandırır.
 *
 * Aynı timestamp değerine sahip mesajlarda id sıralamayı kararlı tutar.
 */
CREATE INDEX idx_messages_chat_history
    ON messages (sender_id, recipient_id, sent_at, id);

/*
 * recipient_id ve is_read üzerinden çalışan okunmamış mesaj
 * sayısı sorgusunu hızlandırır.
 */
CREATE INDEX idx_messages_recipient_read
    ON messages (recipient_id, is_read);