package com.works.patimati.mapper;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * İlan düzenlenirken <b>mikroçip numarası silinmemeli</b>.
 *
 * <p><b>Neden ayrı bir bekçi:</b> {@code updateEntity} tasarımı gereği gelen
 * isteği olduğu gibi yazar — 19 alanın 18'i öyle. Mikroçip <b>tek istisna</b>
 * ve istisnanın sebebi kodun dışında: {@code AdResponse} numarayı geri
 * döndürmüyor (yerine türetilmiş {@code microchipped} bayrağı var), dolayısıyla
 * düzenleme formu onu hiç göremiyor ve kaydederken zorunlu olarak boş
 * gönderiyor. İstisna kaldırılırsa <b>her düzenleme numarayı siler</b> ve
 * kullanıcı yalnız başlığını düzelttiğinde hayvanın kimliğini kaybeder.
 *
 * <p>Kusur bugün kullanıcıya <i>henüz</i> ulaşmıyor çünkü düzenleme ekranı
 * yok — bekçi tam da bu yüzden ekrandan <b>önce</b> yazıldı: ekran gelince
 * kusur sessizce canlıya çıkardı.
 */
class IlanDuzenlemeMikrocipTest {

    private static final String MIKROCIP = "985112345678901";

    private AdMapper adMapper;

    @BeforeEach
    void setUp() {
        adMapper = new AdMapper();
    }

    @Test
    @DisplayName("Numara gönderilmezse KORUNUR — düzenleme onu silmez")
    void bosGelenNumaraMevcuduSilmemeli() {
        Ad ad = mikrocipliIlan();

        adMapper.updateEntity(ad, istek(null));

        assertThat(ad.getMicrochipNumber())
                .withFailMessage("""
                        Düzenleme mikroçip numarasını SİLDİ. Form numarayı hiç
                        göremiyor (AdResponse döndürmüyor), dolayısıyla her
                        kaydetme onu boş gönderir: kullanıcı yalnız başlığını
                        düzelttiğinde hayvanın kimliği kayboluyor demektir.""")
                .isEqualTo(MIKROCIP);
    }

    @Test
    @DisplayName("Numara silinmeyince 'microchipped' bayrağı da FALSE'a dönmez")
    void bayrakSessizceFalseADonmemeli() {
        Ad ad = mikrocipliIlan();

        adMapper.updateEntity(ad, istek(null));
        AdResponse cevap = adMapper.toResponse(ad);

        // İkinci zarar: bayrak numaradan TÜRETİLİYOR (AdMapper toResponse).
        // Numara düşerse ilan "mikroçipsiz" görünür — kullanıcıya yansıyan yüz
        // budur ve alan silinmesinden bağımsız olarak ayrıca sınanır.
        assertThat(cevap.microchipped())
                .withFailMessage("""
                        Düzenleme sonrası ilan "mikroçipsiz" görünüyor. Bayrak
                        numaradan türetildiği için numara silindiğinde sessizce
                        false'a döner; kullanıcı hiçbir uyarı görmez.""")
                .isTrue();
    }

    @Test
    @DisplayName("Yalnız boşluktan oluşan numara da mevcudu silmez")
    void bosluklardanIbaretNumaraSilmemeli() {
        Ad ad = mikrocipliIlan();

        adMapper.updateEntity(ad, istek("   "));

        // Kural tek cümle: BOŞA NORMALLEŞEN DEĞER ALANA DOKUNMAZ. Boş dizgi
        // ile null'ı ayırmak, formun hangisini gönderdiğine bağlı sessiz bir
        // ikinci davranış üretirdi.
        assertThat(ad.getMicrochipNumber()).isEqualTo(MIKROCIP);
    }

    @Test
    @DisplayName("Gerçek bir numara gelirse ÜZERİNE YAZILIR — alan donmuş değil")
    void doluGelenNumaraUzerineYazmali() {
        Ad ad = mikrocipliIlan();

        adMapper.updateEntity(ad, istek(" 900 111 222 333 444 "));

        // İstisnanın sınırı: "hiç yazma" değil, "boş gelirse yazma". Sahip
        // yanlış girdiği numarayı DÜZELTEBİLMELİ.
        assertThat(ad.getMicrochipNumber())
                .withFailMessage("""
                        Dolu numara yazılmadı. İstisna fazla genişletilmiş
                        olmalı: sahip yanlış girdiği numarayı artık
                        düzeltemiyor.""")
                .isEqualTo("900111222333444");
    }

    @Test
    @DisplayName("Numarası olmayan ilan, boş istekle mikroçipli görünmeye başlamaz")
    void numarasizIlanMikrocipliGorunmemeli() {
        Ad ad = mikrocipliIlan();
        ad.setMicrochipNumber(null);

        adMapper.updateEntity(ad, istek(null));

        assertThat(ad.getMicrochipNumber()).isNull();
        assertThat(adMapper.toResponse(ad).microchipped()).isFalse();
    }

    // ---------------------------------------------------------------------
    // Fikstür
    // ---------------------------------------------------------------------

    private Ad mikrocipliIlan() {
        Ad ad = new Ad();
        ad.setTitle("Kayıp tekir kedi");
        ad.setAdType(Ad.AdType.LOST);
        ad.setSpecies(Species.CAT);
        ad.setMicrochipNumber(MIKROCIP);
        return ad;
    }

    /** Düzenleme formunun gerçekte gönderdiği istek: numara alanı hariç dolu. */
    private AdUpdateRequest istek(String mikrocipNumarasi) {
        return new AdUpdateRequest(
                "Kayıp tekir kedi (güncellendi)",
                "Yeni görüldüğü yer eklendi",
                Ad.AdType.LOST,
                Species.CAT,
                null,
                Set.of(PetColor.BROWN),
                PetGender.UNKNOWN,
                AgeGroup.ADULT,
                CoatPattern.UNKNOWN,
                PresenceStatus.UNKNOWN,
                null,
                null,
                EyeColor.UNKNOWN,
                PresenceStatus.UNKNOWN,
                PresenceStatus.UNKNOWN,
                mikrocipNumarasi,
                LocalDate.of(2026, 8, 1),
                "Sol kulakta çentik",
                new BigDecimal("40.195000"),
                new BigDecimal("29.060000"));
    }
}
