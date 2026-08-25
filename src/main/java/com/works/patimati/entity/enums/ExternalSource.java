package com.works.patimati.entity.enums;

/**
 * Bir external_source_posts kaydının hangi platformdan geldiği.
 *
 * <p>Tek değerle başlıyor ama mimari Instagram'a kilitli değil — ileride
 * X/Facebook/manuel import eklenirse bu enum'a yeni değer eklemek yeterli
 * olacak şekilde tasarlandı (bkz. Faz 2 blueprint §53).
 */
public enum ExternalSource {
    INSTAGRAM
}
