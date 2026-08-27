package com.works.patimati.entity.enums;

/**
 * Bir POI kaydının nereden geldiği — dış senkronizasyon mı, elle giriş mi,
 * yoksa platforma kayıtlı bir hesap mı.
 *
 * <p>{@code PLATFORM}, {@code points_of_interest} tablosunda bir SATIR
 * DEĞİLDİR -- {@link com.works.patimati.service.PoiService}'in
 * VetClinic/PetShop/Shelter tablolarından haritaya kattığı, gerçek bir
 * hesaba bağlı sonuçları işaretler (bkz. {@code PoiResponse.refId}).</p>
 */
public enum PoiSource {
    OSM,
    MANUAL,
    PLATFORM
}
