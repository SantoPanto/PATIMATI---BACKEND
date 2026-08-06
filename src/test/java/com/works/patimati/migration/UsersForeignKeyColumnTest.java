package com.works.patimati.migration;

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
 * Migration dosyalarındaki {@code REFERENCES users(...)} ifadelerinin,
 * {@code User} entity'sinin birincil anahtar kolonuyla aynı adı kullandığını
 * doğrular.
 *
 * <p><b>Neden bu test var:</b> 2026-08-06'da boş bir veritabanında uygulama
 * hiç açılmıyordu. Sebep, zincirin anahtarı İKİ FARKLI isimle kullanmasıydı:
 *
 * <pre>
 *   V1, V4, V7 -> REFERENCES users (id)
 *   V6         -> REFERENCES users (uid)
 *   User.java  -> private Long uid;      (yani gerçek kolon adı: uid)
 * </pre>
 *
 * Sonuç: taze kurulumda migration zinciri
 * {@code column "uid" referenced in foreign key constraint does not exist}
 * hatasıyla duruyordu. Mevcut makinelerde görünmüyordu, çünkü tablolar
 * vaktiyle {@code ddl-auto=update} ile entity'ye bakılarak oluşmuştu.
 *
 * <p>Test, beklenen kolon adını <b>entity'den okur</b>; yani ileride anahtarın
 * adı değiştirilirse test kendiliğinden yeni ada göre kontrol eder, elle
 * güncellenmesi gerekmez.
 *
 * <p>Veritabanı gerektirmez: dosyaları metin olarak tarar.
 */
class UsersForeignKeyColumnTest {

    private static final Path ENTITY =
            Paths.get("src", "main", "java", "com", "works", "patimati", "entity", "User.java");
    private static final Path MIGRATIONS =
            Paths.get("src", "main", "resources", "db", "migration");

    /** REFERENCES users (kolon) — boşluk ve büyük/küçük harf serbest. */
    private static final Pattern USERS_FK =
            Pattern.compile("(?i)REFERENCES\\s+users\\s*\\(\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\)");

    /** "private <Tip> <ad>;" — @Id ile ilk noktalı virgül arasındaki alan adı. */
    private static final Pattern ALAN_ADI =
            Pattern.compile("private\\s+\\w+\\s+(\\w+)\\s*$");

    /** @Column(name = "...") — yalnızca @Id alanının KENDİ bildirimi içinde aranır. */
    private static final Pattern KOLON_ADI =
            Pattern.compile("@Column\\s*\\([^)]*name\\s*=\\s*\"([^\"]+)\"");

    @Test
    @DisplayName("Migration'lardaki users yabancı anahtarları entity'nin PK kolonuyla aynı olmalı")
    void usersYabanciAnahtarlariEntityIleAyniOlmali() throws IOException {
        String entity = Files.readString(ENTITY, StandardCharsets.UTF_8);

        // @Id ile onu izleyen İLK noktalı virgül arası = birincil anahtar alanının
        // kendi bildirimi. Aramayı bu dilimle sınırlamak şart: aksi hâlde bir
        // sonraki alanın @Column(name=...) etiketi yakalanıyor (ilk yazımda oldu,
        // test "google_id" bulup yanlış yere düştü).
        int idIndex = entity.indexOf("@Id");
        if (idIndex < 0) {
            throw new IllegalStateException("User.java içinde @Id bulunamadı; test güncellenmeli");
        }
        int noktaliVirgul = entity.indexOf(';', idIndex);
        String pkBildirimi = entity.substring(idIndex, noktaliVirgul);

        Matcher kolon = KOLON_ADI.matcher(pkBildirimi);
        Matcher alan = ALAN_ADI.matcher(pkBildirimi.strip());

        String beklenenKolon;
        if (kolon.find()) {
            beklenenKolon = kolon.group(1);
        } else if (alan.find()) {
            beklenenKolon = alan.group(1);
        } else {
            throw new IllegalStateException(
                    "User.java içindeki @Id alanının adı çözülemedi; test güncellenmeli");
        }

        List<String> ihlaller = new ArrayList<>();
        try (Stream<Path> dosyalar = Files.walk(MIGRATIONS)) {
            for (Path dosya : dosyalar.filter(p -> p.toString().endsWith(".sql")).sorted().toList()) {
                String icerik = Files.readString(dosya, StandardCharsets.UTF_8);
                Matcher m = USERS_FK.matcher(icerik);
                while (m.find()) {
                    if (!m.group(1).equalsIgnoreCase(beklenenKolon)) {
                        ihlaller.add(String.format("%s  ->  REFERENCES users(%s)",
                                dosya.getFileName(), m.group(1)));
                    }
                }
            }
        }

        assertThat(ihlaller)
                .withFailMessage("""
                        Migration'lar users tablosunun anahtarını entity'den FARKLI bir \
                        adla kullanıyor.

                        User entity'sinin birincil anahtar kolonu: "%s"
                        Uyuşmayanlar:
                        %s

                        Boş bir veritabanında migration zinciri şu hatayla durur:
                          column "..." referenced in foreign key constraint does not exist

                        Mevcut makinelerde görünmez, çünkü tablolar ddl-auto=update ile \
                        entity'ye bakılarak oluşmuştur.
                        """, beklenenKolon, String.join("\n", ihlaller))
                .isEmpty();
    }
}
