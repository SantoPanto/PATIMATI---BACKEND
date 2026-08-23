package com.works.patimati.entity.enums;

/**
 * Bir Instagram gönderisinin PatiMati hesabıyla nasıl ilişkilendirildiği.
 *
 * <p>Aynı gönderi üçü ile birden tespit edilebilir — bu yüzden
 * external_source_events ayrı satırlar tutar, external_source_posts ise
 * tek kalır (kanonik kimlik, bkz. blueprint §18).
 */
public enum TriggerType {
    POST_TAG,
    CAPTION_MENTION,
    COMMENT_MENTION
}
