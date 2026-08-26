package com.works.patimati.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * {@code PetShopProductReviewGuvenlikRegresyonTest}'in birebir aynısı, barınak
 * yorumları için: SecurityConfig'te {@code /api/shelters} yalnızca GET
 * permitAll olmalı, PUT (yazma) kimlik doğrulaması istemeli. Ayrıca yeni
 * herkese açık alt-kaynak {@code GET /api/shelters/{id}/adoptions}'ın da
 * GET-only permitAll matcher'ına düştüğünü kanıtlayan üçüncü bir vaka
 * içerir -- ekstra bir SecurityConfig satırı GEREKMEDİĞİNİ doğrular
 * ({@code /api/petshops/{id}/products} ile aynı desen).
 *
 * <p>Bu test, {@code ShelterController}/{@code ShelterDirectoryController}/
 * {@code ShelterReviewController} henüz mevcut OLMASA bile SecurityConfig'in
 * URL deseni eşleşmesini ölçer -- Spring Security filtre zinciri, hedef
 * controller var olmasa bile 401/403 kararını dispatcher'a ulaşmadan ÖNCE
 * verir.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class ShelterReviewGuvenlikRegresyonTest {

    @MockitoBean
    private DataSource dataSource;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Jetonsuz PUT /api/shelters/1/reviews/me 401/403 ile reddedilir")
    void jetonsuzYorumYazmaReddedilir() throws Exception {
        MvcResult sonuc = mockMvc.perform(put("/api/shelters/1/reviews/me")
                        .contentType("application/json")
                        .content("{\"rating\":5,\"comment\":\"Harika\"}"))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz PUT /api/shelters/1/reviews/me %d aldı. SecurityConfig'te
                        "/api/shelters" ve "/api/shelters/**" satırları HTTP metodu ayrımı
                        yapmadan permitAll() listesindeyse bu istek kimlik doğrulaması
                        olmadan geçer -- herkes kimliksiz yorum/puan atabilir demektir.""",
                        sonuc.getResponse().getStatus())
                .isIn(401, 403);
    }

    @Test
    @DisplayName("Jetonsuz GET /api/shelters/1/reviews hâlâ açık kalır (401/403 DEĞİL)")
    void jetonsuzYorumOkumaAcikKalir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/shelters/1/reviews")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz GET /api/shelters/1/reviews %d aldı. Bu uç herkese açık
                        kalmalı (yorum listesi okuma) -- güvenlik düzeltmesi GET'i de
                        kilitlemiş olabilir.""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }

    @Test
    @DisplayName("Jetonsuz GET /api/shelters/1/adoptions hâlâ açık kalır (401/403 DEĞİL)")
    void jetonsuzIlanListesiAcikKalir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/shelters/1/adoptions")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz GET /api/shelters/1/adoptions %d aldı. Bu yeni alt-kaynak
                        GET-only "/api/shelters/**" permitAll matcher'ına düşmeli -- ekstra
                        bir SecurityConfig satırı gerekmiyor (bkz. /api/petshops/{id}/products
                        ile aynı desen).""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }
}
