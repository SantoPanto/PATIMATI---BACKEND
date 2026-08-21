package com.works.patimati.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
 */
class GlobalExceptionHandlerTest {

    /** Canlıda kullanıcı ekranına basılmış gerçek mesajın çekirdeği. */
    private static final String HAM_SQL_MESAJI =
            "could not execute statement [ERROR: value too long for type character varying(255)] "
                    + "[insert into ads (active,ad_type,description) values (?,?,?)]";

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("POST", "/api/ads");
    }

    @Test
    @DisplayName("Veri bütünlüğü ihlali 400 döner ve ham SQL metni gövdede yer almaz")
    void dataIntegrityViolationSqlSizdirmaz() {
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
    @DisplayName("ErrorResponse istisnaları (404 gibi) kendi durum kodunu korur")
    void errorResponseDurumKodunuKorur() {
        ProblemDetail detail = handler.handleGenericException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "kayıt bulunamadı"),
                request
        ).getBody();

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
