package com.works.patimati.entity.enums;

/**
 * Bir ilanın AI analizinin hangi aşamada olduğu.
 *
 * <p>Akış asenkrondur (entegrasyon sözleşmesi §1): ilan kaydedildiği anda
 * {@link #PENDING} olur ve kullanıcı beklemeden ilanını görür. AI sonucu
 * kuyruktan döndüğünde {@link #DONE} ya da {@link #FAILED} yazılır.
 *
 * <p>{@link #FAILED} durumunda ilan yayında kalır — AI bir ektir, ürünün
 * kalbi değildir. Yalnızca eşleştirmeye aday olarak girmez.
 */
public enum AiStatus {
    PENDING,
    DONE,
    FAILED
}
