package com.works.patimati.entity.enums;

/**
 * Bir veteriner klinğinin baktığı hayvan türleri -- bilerek {@link Species}
 * (yalnızca {@code CAT, DOG, UNKNOWN}, ilan/kayıp-bulundu eşleştirmesine
 * bağlı) ile AYRI: gerçek hayatta bir veterinerin ilgilendiği tür yelpazesi
 * çok daha geniş, {@code Species}'e hiç dokunulmuyor.
 */
public enum AnimalType {
    DOG, CAT, BIRD, RABBIT, RODENT, REPTILE, FISH, FARM_ANIMAL, EXOTIC, OTHER
}
