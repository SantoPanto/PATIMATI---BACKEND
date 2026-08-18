package com.works.patimati.controller;

import com.works.patimati.service.AiMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * İlan oluşturma ekranının analiz ucu — cevabı taşır, iç ayrıntıyı taşımaz.
 *
 * <p><b>Neden bu uç var (A1):</b> tarayıcı AI'yı doğrudan çağırıyordu, bu yüzden
 * AI'nın uçlarına kimlik konulamıyordu. Kimlik denemesi, o çağrı backend'e
 * taşınmadan yapılamaz.
 *
 * <p><b>Kapsam sınırı:</b> burada güvenlik filtresi ÇALIŞMIYOR (standalone
 * kurulum). "Bu uç kimlik istiyor" iddiası {@code AiAnalizUcuKimlikIstiyorTest}
 * ile gerçek filtre zinciri üzerinden ölçülüyor; ikisini karıştırmamak gerekir,
 * yoksa buradaki yeşil "uç korunuyor" sanılır.
 */
class AiAnalyzeControllerTest {

    private AiMatchService aiMatchService;
    private MockMvc mockMvc;

    @BeforeEach
    void hazirla() {
        aiMatchService = mock(AiMatchService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AiAnalyzeController(aiMatchService)).build();
    }

    @Test
    @DisplayName("AI'nın cevabı OLDUĞU GİBİ aktarılır — tanımadığımız alan bile düşmez")
    void cevapOlduguGibiAktarilir() throws Exception {
        Map<String, Object> aiCevabi = new LinkedHashMap<>();
        aiCevabi.put("species", "cat");
        aiCevabi.put("species_confidence", 0.97);
        aiCevabi.put("breed", null);
        aiCevabi.put("labels", List.of("cat", "tabby"));
        // AI'ya yarın eklenecek bir alanı temsil eder: backend alanları tek tek
        // saysaydı bu sessizce düşer ve ekran alan alan kırılırdı.
        aiCevabi.put("yarin_eklenen_alan", "değer");
        when(aiMatchService.analyzeImage(any())).thenReturn(aiCevabi);

        mockMvc.perform(multipart("/api/ai/analyze").file(fotograf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value("cat"))
                .andExpect(jsonPath("$.species_confidence").value(0.97))
                .andExpect(jsonPath("$.labels[1]").value("tabby"))
                .andExpect(jsonPath("$.yarin_eklenen_alan").value("değer"));
    }

    @Test
    @DisplayName("Boş dosya 400 döner — AI'ya boşuna gidilmez")
    void bosDosyaReddedilir() throws Exception {
        mockMvc.perform(multipart("/api/ai/analyze")
                        .file(new MockMultipartFile("file", "bos.jpg", "image/jpeg", new byte[0])))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AI patlarsa 502 döner ve iç ayrıntı İSTEMCİYE SIZMAZ")
    void icHataSizmaz() throws Exception {
        String icAyrinti = "jdbc:postgresql://10.0.0.7:5432/patimati_db kullanici=postgres";
        when(aiMatchService.analyzeImage(any())).thenThrow(new RuntimeException(icAyrinti));

        MvcResult sonuc = mockMvc.perform(multipart("/api/ai/analyze").file(fotograf()))
                .andExpect(status().isBadGateway())
                .andReturn();

        String govde = sonuc.getResponse().getContentAsString();
        assertThat(govde)
                .withFailMessage("""
                        İç hata ayrıntısı istemciye gitti: %s

                        Bu kusur aynı depoda bir kez yaşandı (AiMatchController
                        e.getMessage()'i gövdeye yazıyordu) ve incelemede
                        bulgu olarak açıldı.""", govde)
                .doesNotContain("jdbc")
                .doesNotContain("10.0.0.7")
                .doesNotContain("postgres");
    }

    @Test
    @DisplayName("AI 2xx dışında cevap verirse 502 döner — boş 200 gösterilmez")
    void aiCevapVermezse502() throws Exception {
        when(aiMatchService.analyzeImage(any())).thenReturn(null);

        mockMvc.perform(multipart("/api/ai/analyze").file(fotograf()))
                .andExpect(status().isBadGateway());
    }

    private MockMultipartFile fotograf() {
        return new MockMultipartFile("file", "kedi.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }
}
