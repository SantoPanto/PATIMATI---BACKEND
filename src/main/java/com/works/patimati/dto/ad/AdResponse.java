package com.works.patimati.dto.ad;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AdResponse(
        Long id,
        String title,
        String description,
        Ad.AdType adType,
        Species species,
        String breed,
        Set<PetColor> colors,
        PetGender gender,
        AgeGroup ageGroup,
        CoatPattern coatPattern,
        PresenceStatus collarStatus,
        PetColor collarColor,
        String collarTagText,
        EyeColor eyeColor,
        PresenceStatus earTagStatus,
        PresenceStatus earNotchStatus,
        boolean microchipped,
        LocalDate lostDate,
        String distinctiveMarks,
        List<String> photoUrls,
        Double latitude,
        Double longitude,
        Long ownerId,
        String ownerDisplayName,
        boolean active,
        Instant createdAt,
        Instant updatedAt,

        /*
         * --- AI analizinin arayüze görünen kısmı ---
         *
         * İkisi BİRLİKTE anlamlıdır ve bu yüzden birlikte dönüyorlar:
         * aiIsPet = null tek başına "hayvan yok" demek değildir, "henüz
         * bakılmadı" da olabilir. Ayrımı aiStatus söyler.
         *
         *   aiStatus=PENDING, aiIsPet=null   → analiz sürüyor, bir şey deme
         *   aiStatus=FAILED,  aiIsPet=null   → analiz edilemedi, bir şey deme
         *   aiStatus=DONE,    aiIsPet=true   → fotoğrafta hayvan görüldü
         *   aiStatus=DONE,    aiIsPet=false  → "bu fotoğrafta kedi/köpek
         *                                       görünmüyor, başka bir fotoğraf
         *                                       ekler misiniz?" denebilir
         *
         * Vektörler ve etiketler BİLEREK dönmüyor: iç veri, arayüze faydası yok
         * ve ilan başına 768 float × fotoğraf sayısı taşımak listeyi şişirir.
         */
        AiStatus aiStatus,
        Boolean aiIsPet,
        Boolean isPosterAllowed,
        Boolean showEmailOnPoster,
        Boolean showPhoneOnPoster
) {
}
