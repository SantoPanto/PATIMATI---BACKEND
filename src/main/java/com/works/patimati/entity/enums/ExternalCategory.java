package com.works.patimati.entity.enums;

/**
 * external_pet_records.category — Instagram içeriğinin AI tarafından
 * sınıflandırıldığı niyet.
 *
 * <p>Bilinçli olarak {@link com.works.patimati.entity.Ad.AdType}'tan AYRI
 * tutulur: IRRELEVANT ve UNCERTAIN yalnızca Instagram tarafında anlamlıdır,
 * çekirdek ilan iş mantığına sızdırılmamalı (Faz 2 blueprint §7).
 */
public enum ExternalCategory {
    LOST,
    FOUND,
    ADOPTION,
    IRRELEVANT,
    UNCERTAIN
}
