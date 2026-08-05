package com.works.patimati.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Native sorgularda <b>parametreye</b> PostgreSQL tip dönüşümü ({@code :param::tip})
 * yazılmasını engeller.
 *
 * <p><b>Neden bu test var:</b> {@code :point::geography} yazıldığında Hibernate
 * parametrenin adını "point::geography" olarak okur ve sorgu <b>çalışma anında</b>
 * şu hatayla patlar:
 *
 * <pre>
 * No parameter named ':point' in query with named parameters [distanceInMeters, point::geography]
 * </pre>
 *
 * <p>Bu hata derlemede görünmez, birim testlerinde görünmez ve iki yerde çağıran
 * kod istisnayı yuttuğu için <b>kullanıcıya da görünmez</b>. 2026-08-05 uçtan uca
 * denemesinde bulunduğunda üç ayrı sorguda vardı ve sonuçları şunlardı:
 *
 * <ul>
 *   <li>{@code findAiCandidates} — AI'ya hiçbir ilan gitmiyordu, ilanlar sessizce
 *       {@code PENDING} kalıyordu.</li>
 *   <li>{@code findUsersNearby} — yakındaki kullanıcılara hiç bildirim gitmiyordu.</li>
 *   <li>{@code findNearbyAds} — yakın ilan sorgusu çalışmıyordu.</li>
 * </ul>
 *
 * <p><b>Doğrusu:</b> {@code CAST(:param AS geography)}.
 * Bir SÜTUN üzerinde {@code a.location::geography} yazmak sorun değildir;
 * yasak olan yalnızca parametreye {@code ::} uygulamaktır.
 *
 * <p>Test veritabanı gerektirmez: kaynak dosyaları metin olarak tarar, bu yüzden
 * CI'da her zaman ve hızlıca çalışır.
 */
class NativeQueryParameterCastTest {

    /** :adiBirParametre :: → yasak. Sütun (nokta içeren) ifadeler eşleşmez. */
    private static final Pattern PARAMETREYE_CAST =
            Pattern.compile(":[a-zA-Z_][a-zA-Z0-9_]*::");

    @Test
    @DisplayName("Hiçbir native sorgu parametreye ':param::tip' biçiminde cast uygulamamalı")
    void parametreyeCastYazilmamali() throws IOException {
        Path kaynakKok = Paths.get("src", "main", "java");
        List<String> ihlaller = new ArrayList<>();

        try (Stream<Path> dosyalar = Files.walk(kaynakKok)) {
            for (Path dosya : dosyalar.filter(p -> p.toString().endsWith(".java")).toList()) {
                String icerik = Files.readString(dosya, StandardCharsets.UTF_8);
                if (!icerik.contains("@Query")) {
                    continue;
                }
                String[] satirlar = icerik.split("\\R");
                for (int i = 0; i < satirlar.length; i++) {
                    String satir = satirlar[i];
                    // Javadoc/yorum satırlarını atla: orada örnek olarak geçebilir.
                    String kirpik = satir.trim();
                    if (kirpik.startsWith("*") || kirpik.startsWith("//") || kirpik.startsWith("/*")) {
                        continue;
                    }
                    Matcher m = PARAMETREYE_CAST.matcher(satir);
                    if (m.find()) {
                        ihlaller.add(String.format(
                                "%s:%d  ->  %s", dosya, i + 1, m.group()));
                    }
                }
            }
        }

        assertThat(ihlaller)
                .withFailMessage("""
                        Native sorguda parametreye tip dönüşümü uygulanmış.

                        Bulunanlar:
                        %s

                        Hibernate ':param::tip' ifadesinde parametre adını
                        'param::tip' olarak okur ve sorgu ÇALIŞMA ANINDA patlar
                        ("No parameter named ':param' ...").

                        Yapılacak: :param::geography  ->  CAST(:param AS geography)
                        (Sütunda ::geography kullanmak serbesttir.)
                        """, String.join("\n", ihlaller))
                .isEmpty();
    }
}
