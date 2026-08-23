package com.works.patimati.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B1 bulgusunun kilidi: veritabanından dönen ham SQL metni istemciye sızmasın.
 *
 * <p>Sızıntı iki kapıdan oluyordu: {@code DataIntegrityViolationException}'ın
 * özel bir işleyicisi yoktu ve genel işleyici {@code getMessage()}'ı aynen
 * istemciye kopyalıyordu. İki test de gerçek sızıntı metniyle (canlıda ekrana
 * basılan {@code could not execute statement ... insert into ads ...}) kurulur;
 * hem yasak içeriğin YOKLUĞU hem güvenli metnin VARLIĞI doğrulanır.
 *
 * <p>Ayrıca yeni {@code Exception.class} catch-all'ının (dosyanın en altında)
 * daha önce hiç yakalanmayan istisnaları tutarlı bir {@code ProblemDetail}
 * ile 500'e çevirdiğini, hâlihazırdaki spesifik handler'ların davranışını
 * DEĞİŞTİRMEDİĞİNİ ve iç detayları istemciye sızdırmadığını doğrular.
 */
class GlobalExceptionHandlerTest {

    /** Canlıda kullanıcı ekranına basılmış gerçek mesajın çekirdeği. */
    private static final String HAM_SQL_MESAJI =
            "could not execute statement [ERROR: value too long for type character varying(255)] "
                    + "[insert into ads (active,ad_type,description) values (?,?,?)]";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mevcutSpesifikHandlerDavranisiDegismiyor() {
        // Katalog listesindeki spesifik handler'lardan biri: yeni catch-all
        // eklenmeden önceki davranışıyla aynı kalmalı (400, kendi mesajıyla).
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ads/1");

        ResponseEntity<ProblemDetail> response =
                handler.handleIllegalArgument(new IllegalArgumentException("geçersiz sayfa boyutu"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getDetail()).isEqualTo("geçersiz sayfa boyutu");
    }

    @Test
    void beklenmeyenHataIcinCatchAll500VeProblemDetailDoner() {
        // Daha önce hiçbir handler'a uymayan bir istisna (ör. gerçek bir NPE)
        // yakalanmıyordu -- artık tutarlı bir ProblemDetail ile 500 döner.
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/ads/1");

        ResponseEntity<ProblemDetail> response =
                handler.handleGenericException(new NullPointerException("beklenmedik null"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInstance()).hasToString("/api/ads/1");
    }

    @Test
    @DisplayName("Veri bütünlüğü ihlali 400 döner ve ham SQL metni gövdede yer almaz")
    void dataIntegrityViolationSqlSizdirmaz() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ads");

        ProblemDetail detail = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException(HAM_SQL_MESAJI),
                request
        ).getBody();

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getDetail())
                .doesNotContain("insert into")
                .doesNotContain("varying(255)")
                .contains("izin verilen sınırı aşıyor");
    }

    @Test
    @DisplayName("Beklenmeyen istisnanın mesajı istemciye kopyalanmaz")
    void beklenmeyenIstisnaMesajiKopyalanmaz() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ads");

        ProblemDetail detail = handler.handleGenericException(
                new IllegalMonitorStateException(HAM_SQL_MESAJI),
                request
        ).getBody();

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getTitle()).isEqualTo("Sunucu Hatası");
        assertThat(detail.getDetail())
                .isEqualTo("Beklenmeyen bir sunucu hatası meydana geldi.")
                .doesNotContain("insert into");
    }

    @Test
    void catchAllHamExceptionMesajiniIstemciyeSizdirmiyor() {
        // İç detay (ör. bir SQL hata metni) client'a çıplak dönmemeli --
        // yalnızca sabit, genel bir mesaj.
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ads");

        ResponseEntity<ProblemDetail> response =
                handler.handleGenericException(new RuntimeException("Duplicate entry 'x' for key PRIMARY"), request);

        assertThat(response.getBody().getDetail())
                .doesNotContain("Duplicate entry")
                .doesNotContain("PRIMARY");
    }

    @Test
    @DisplayName("ErrorResponse istisnaları (404 gibi) kendi durum kodunu korur")
    void errorResponseDurumKodunuKorur() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ads");

        ProblemDetail detail = handler.handleGenericException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "kayıt bulunamadı"),
                request
        ).getBody();

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
