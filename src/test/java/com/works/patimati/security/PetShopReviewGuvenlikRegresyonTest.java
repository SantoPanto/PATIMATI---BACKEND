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
 * {@code ShelterReviewGuvenlikRegresyonTest}'in birebir aynısı, dükkan
 * seviyesi petshop yorumları için: SecurityConfig'te {@code /api/petshops}
 * yalnızca GET permitAll olmalı, PUT (yazma) kimlik doğrulaması istemeli.
 * Üçüncü vaka mevcut {@code GET /api/petshops/{id}/products} alt-kaynağının
 * hâlâ GET-only permitAll matcher'ına düştüğünü kanıtlar -- yeni yorum
 * controller'ı için SecurityConfig'e ekstra satır GEREKMEDİĞİNİ doğrular.
 *
 * <p>Bu test, {@code PetShopReviewController} henüz mevcut OLMASA bile
 * SecurityConfig'in URL deseni eşleşmesini ölçer -- Spring Security filtre
 * zinciri, hedef controller var olmasa bile 401/403 kararını dispatcher'a
 * ulaşmadan ÖNCE verir.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class PetShopReviewGuvenlikRegresyonTest {

    @MockitoBean
    private DataSource dataSource;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Jetonsuz PUT /api/petshops/1/reviews/me 401/403 ile reddedilir")
    void jetonsuzYorumYazmaReddedilir() throws Exception {
        MvcResult sonuc = mockMvc.perform(put("/api/petshops/1/reviews/me")
                        .contentType("application/json")
                        .content("{\"rating\":5,\"comment\":\"Harika\"}"))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz PUT /api/petshops/1/reviews/me %d aldı. SecurityConfig'te
                        "/api/petshops" ve "/api/petshops/**" satırları HTTP metodu ayrımı
                        yapmadan permitAll() listesindeyse bu istek kimlik doğrulaması
                        olmadan geçer -- herkes kimliksiz yorum/puan atabilir demektir.""",
                        sonuc.getResponse().getStatus())
                .isIn(401, 403);
    }

    @Test
    @DisplayName("Jetonsuz GET /api/petshops/1/reviews hâlâ açık kalır (401/403 DEĞİL)")
    void jetonsuzYorumOkumaAcikKalir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/petshops/1/reviews")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz GET /api/petshops/1/reviews %d aldı. Bu uç herkese açık
                        kalmalı (yorum listesi okuma) -- güvenlik düzeltmesi GET'i de
                        kilitlemiş olabilir.""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }

    @Test
    @DisplayName("Jetonsuz GET /api/petshops/1/products hâlâ açık kalır (401/403 DEĞİL)")
    void jetonsuzUrunListesiAcikKalir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/petshops/1/products")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz GET /api/petshops/1/products %d aldı. Bu alt-kaynak
                        GET-only "/api/petshops/**" permitAll matcher'ına düşmeli -- yeni
                        yorum controller'ı eklense de ekstra bir SecurityConfig satırı
                        gerekmiyor.""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }
}
