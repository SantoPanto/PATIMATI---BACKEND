package com.works.patimati.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Aynı göç numarasından <b>iki dosya</b> bulunmadığını doğrular.
 *
 * <p><b>Neden bu test var:</b> 19.08'de <b>aynı gün iki kez</b> aynı numara iki
 * ayrı dalda kullanıldı. Önce iki açık istek birden {@code V12} yazdı; o çözülür
 * çözülmez bu kez ikisi birden {@code V13} yazdı. Sebep dikkatsizlik değil,
 * <b>yapı</b>: göç numarası tek ve paylaşılan bir sayaç, göçleri de dört ayrı
 * kişi yazıyor. Herkes "sıradaki numara" diye aynı sayıyı seçiyor.
 *
 * <p><b>Bedeli neden ağır:</b> Flyway aynı numaradan iki dosya görünce göç
 * zincirini hiç başlatmıyor — uygulama <b>açılmıyor</b>. Yani hata birleştirme
 * anında değil, <b>dağıtım anında</b> ve <b>her ortamda birden</b> ortaya çıkıyor.
 *
 * <p><b>Bu testin göremediği durum — bilerek:</b> test yalnız <b>bu ağaçtaki</b>
 * dosyalara bakar. Bir dal ortak dalda zaten alınmış bir numarayı kullanıyorsa,
 * o dalın kendi ağacında çakışma görünmez. O durumu CI'daki
 * <i>"Göç numarası ortak dalda alınmış mı"</i> adımı yakalar; ikisi birlikte
 * çalışır. 19.08'de bekleyen istek tam olarak bu ikinci sınıftaydı.
 *
 * <p>Veritabanı gerektirmez: dosya adlarını okur.
 */
class GocNumarasiCakismasiTest {

    private static final Path GOCLER =
            Paths.get("src", "main", "resources", "db", "migration");

    /** {@code V<numara>__<aciklama>.sql} — yinelenebilir {@code R__} göçlerinde numara yoktur. */
    private static final Pattern SURUMLU_GOC =
            Pattern.compile("^V([0-9][0-9_.]*)__.+\\.sql$");

    /**
     * Flyway {@code _} ile {@code .}'yı aynı ayırıcı sayar ve baştaki sıfırları
     * yok sayar: {@code V1_1}, {@code V1.1} ve {@code V01.1} <b>aynı sürümdür</b>.
     * Dosya adına bakıp "farklı" demek bu yüzden yetmiyor.
     */
    private static String surumuNormalize(String ham) {
        String[] parcalar = ham.replace('_', '.').split("\\.");
        List<String> temiz = new ArrayList<>();
        for (String p : parcalar) {
            if (p.isEmpty()) {
                continue;
            }
            temiz.add(String.valueOf(Long.parseLong(p)));
        }
        return String.join(".", temiz);
    }

    @Test
    @DisplayName("Aynı göç numarasından iki dosya yok")
    void ayniNumaradanIkiDosyaYok() throws IOException {
        Map<String, List<String>> surumeGore = new LinkedHashMap<>();
        int taranan = 0;

        try (Stream<Path> akis = Files.list(GOCLER)) {
            for (Path p : akis.sorted().toList()) {
                String ad = p.getFileName().toString();
                Matcher m = SURUMLU_GOC.matcher(ad);
                if (!m.matches()) {
                    continue;
                }
                taranan++;
                surumeGore.computeIfAbsent(surumuNormalize(m.group(1)), k -> new ArrayList<>()).add(ad);
            }
        }

        // KARŞILAŞTIRMA: hiç dosya bulunmadıysa aşağıdaki iddia boş yere yeşil yanar.
        assertThat(taranan)
                .withFailMessage("""
                        %s altında sürümlü göç dosyası bulunamadı.
                        O hâlde "çakışma yok" sonucu dosyaların temiz olduğunu
                        DEĞİL, taramanın yanlış yere baktığını gösteriyor.""", GOCLER)
                .isGreaterThan(0);

        List<String> cakisan = surumeGore.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> "  V" + e.getKey() + " -> " + String.join(" · ", e.getValue()))
                .toList();

        assertThat(cakisan)
                .withFailMessage("""
                        Aynı göç numarasından birden fazla dosya var (%d dosya tarandı):
                        %s
                        Flyway bunu görünce göç zincirini hiç başlatmaz ve UYGULAMA AÇILMAZ.
                        Sonra birleşen dosya bir sonraki BOŞ numaraya taşınmalı; taşımadan
                        önce numarayı ekibe duyur, yoksa aynı çakışma tekrar doğar.""",
                        taranan, String.join(System.lineSeparator(), cakisan))
                .isEmpty();
    }
}
