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
 * {@code VetClinicReviewGuvenlikRegresyonTest}'in birebir aynısı, petshop
 * ürün yorumları için: SecurityConfig'te {@code /api/petshop-products}
 * yalnızca GET permitAll olmalı, PUT (yazma) kimlik doğrulaması istemeli.
 *
 * <p>Bu test, {@code PetShopProductPublicController} henüz mevcut OLMASA bile
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
class PetShopProductReviewGuvenlikRegresyonTest {

    @MockitoBean
    private DataSource dataSource;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Jetonsuz PUT /api/petshop-products/1/reviews/me 401/403 ile reddedilir")
    void jetonsuzYorumYazmaReddedilir() throws Exception {
        MvcResult sonuc = mockMvc.perform(put("/api/petshop-products/1/reviews/me")
                        .contentType("application/json")
                        .content("{\"rating\":5,\"comment\":\"Harika\"}"))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz PUT /api/petshop-products/1/reviews/me %d aldı. SecurityConfig'te
                        "/api/petshop-products" ve "/api/petshop-products/**" satırları HTTP metodu
                        ayrımı yapmadan permitAll() listesindeyse bu istek kimlik doğrulaması
                        olmadan geçer -- herkes kimliksiz yorum/puan atabilir demektir.""",
                        sonuc.getResponse().getStatus())
                .isIn(401, 403);
    }

    @Test
    @DisplayName("Jetonsuz GET /api/petshop-products/1/reviews hâlâ açık kalır (401/403 DEĞİL)")
    void jetonsuzYorumOkumaAcikKalir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/petshop-products/1/reviews")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Jetonsuz GET /api/petshop-products/1/reviews %d aldı. Bu uç herkese açık
                        kalmalı (yorum listesi okuma) -- güvenlik düzeltmesi GET'i de
                        kilitlemiş olabilir.""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }
}
