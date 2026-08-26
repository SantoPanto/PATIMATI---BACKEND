package com.works.patimati.municipality;

import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Belediye panelinin kapsamı — <b>modülün tek veri sızıntısı riski burada</b>.
 *
 * <p>NEDEN VAR: yetki kontrolü ("kim girebilir") ile kapsam ("ne görebilir")
 * ayrı şeyler. {@code /api/municipality/**} yalnız kurumlara açık olsa bile,
 * ilçe istekten okunursa Bursa/Nilüfer belediyesi {@code ?ilce=Çankaya} yazıp
 * başka belediyenin ihbarlarını ve ilan verisini okur. Bu yüzden ilçe SUNUCUDA,
 * oturumdaki kullanıcıdan türetiliyor ve bu sınıf onu ölçüyor.
 *
 * <p>İkinci ölçülen şey: <b>kapsam çözülemediğinde ne oluyor.</b> Sessizce
 * "ilçe filtresi yok" durumuna düşmek en tehlikeli davranış olurdu — yanlış
 * yapılandırılmış TEK bir kurum hesabı ülkenin tamamını görürdü. Reddedilmeli.
 */
class BelediyeKapsamCozucuTest {

    private static final String EPOSTA = "nilufer@belediye.local";

    private UserRepository userRepository;
    private MunicipalityScopeService kapsamCozucu;

    @BeforeEach
    void kur() {
        userRepository = mock(UserRepository.class);
        kapsamCozucu = new MunicipalityScopeService(userRepository);
    }

    @AfterEach
    void temizle() {
        SecurityContextHolder.clearContext();
    }

    /**
     * JwtAuthFilter'ın kurduğu kimliğin birebir aynısı: principal E-POSTA
     * (UserDetails değil), yetki "ROLE_" + jetondaki rol. Düzenek burada
     * gerçeğinden saparsa test başka bir şeyi ölçmüş olur.
     */
    private void oturumAc(String rolClaim) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        EPOSTA, null, List.of(new SimpleGrantedAuthority("ROLE_" + rolClaim))));
    }

    private void kullanici(User.Role rol, String ilce) {
        User kullanici = new User();
        kullanici.setUid(7L);
        kullanici.setEmail(EPOSTA);
        kullanici.setEnabled(true);
        kullanici.setRole(rol);
        kullanici.setInstitutionName("Nilüfer Belediyesi");
        kullanici.setInstitutionCity("Bursa");
        kullanici.setInstitutionDistrict(ilce);
        when(userRepository.findByEmail(EPOSTA)).thenReturn(Optional.of(kullanici));
    }

    @Test
    @DisplayName("Kurum hesabının kapsamı KENDİ ilçesidir, istekten okunmaz")
    void kurum_kendi_ilcesini_alir() {
        kullanici(User.Role.INSTITUTION, "Nilüfer");
        oturumAc("INSTITUTION");

        MunicipalityScopeService.BelediyeKapsami kapsam = kapsamCozucu.mevcutKapsam();

        assertThat(kapsam.ilce()).isEqualTo("Nilüfer");
        assertThat(kapsam.il()).isEqualTo("Bursa");
        assertThat(kapsam.kurumKullaniciId()).isEqualTo(7L);
        assertThat(kapsam.kurumAdi()).isEqualTo("Nilüfer Belediyesi");
    }

    @Test
    @DisplayName("Kapsam çözücünün ilçeyi dışarıdan alacak bir yolu YOK")
    void ilce_disaridan_verilemiyor() {
        // Bu iddia gövdeyle değil BİÇİMLE ölçülüyor: sınıfın kamuya açık hiçbir
        // metodu parametre almıyor. Biri ileride "kolaylık olsun" diye
        // mevcutKapsam(String ilce) eklerse, çağıran uç o ilçeyi istekten
        // geçirebilir hâle gelir ve kapsam güvencesi sessizce kaybolur.
        boolean parametreAlanVar = Arrays.stream(
                        MunicipalityScopeService.class.getDeclaredMethods())
                .filter(metot -> Modifier.isPublic(metot.getModifiers()))
                .anyMatch(metot -> metot.getParameterCount() > 0);

        assertThat(parametreAlanVar)
                .withFailMessage("""
                        MunicipalityScopeService'e parametre alan kamusal bir metot
                        eklenmiş. Kapsamın tek kaynağı OTURUM olmalı: ilçe dışarıdan
                        verilebiliyorsa bir belediye hesabı parametreyi değiştirerek
                        başka ilçenin verisini okur — uç "yalnız kurumlara açık" olsa bile.""")
                .isFalse();
    }

    @Test
    @DisplayName("Sıradan kullanıcı kapsam alamaz")
    void siradan_kullanici_reddedilir() {
        kullanici(User.Role.USER, "Nilüfer");
        oturumAc("USER");

        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("İlçesi atanmamış kurum REDDEDİLİR — filtresiz sorguya düşülmez")
    void ilcesiz_kurum_reddedilir() {
        kullanici(User.Role.INSTITUTION, null);
        oturumAc("INSTITUTION");

        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .withFailMessage("""
                        İlçesi olmayan kurum hesabı kapsam alabildi. Bu, panel
                        sorgusunun ilçe filtresiz koşması demektir: tek yanlış
                        yapılandırılmış hesap tüm ülkenin verisini görür.""")
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Boşluktan ibaret ilçe de yok sayılır")
    void bosluk_ilce_reddedilir() {
        kullanici(User.Role.INSTITUTION, "   ");
        oturumAc("INSTITUTION");

        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("İlçe kırpılarak dönüyor — kıyas SQL'de LOWER ile yapılacak")
    void ilce_kirpiliyor() {
        kullanici(User.Role.INSTITUTION, "  Nilüfer  ");
        oturumAc("INSTITUTION");

        // Kırpma burada; küçültme BİLEREK yok. Java'nın Türkçe yerelindeki
        // toLowerCase ile PostgreSQL'in lower()'ı aynı sonucu vermeyebiliyor
        // ("I" sırasıyla "ı" ve "i" olur); iki taraf ayrı motorda küçültülürse
        // aynı ilçe eşleşmeyebilir. Kıyas tek motorda, veritabanında yapılmalı.
        assertThat(kapsamCozucu.mevcutIlce()).isEqualTo("Nilüfer");
    }

    @Test
    @DisplayName("İlçesi dolu ADMIN kabul edilir (prova için)")
    void ilcesi_dolu_yonetici_kabul_edilir() {
        kullanici(User.Role.ADMIN, "Nilüfer");
        oturumAc("ADMIN");

        assertThat(kapsamCozucu.mevcutIlce()).isEqualTo("Nilüfer");
    }

    @Test
    @DisplayName("İlçesi olmayan ADMIN de reddedilir — muafiyet yok")
    void ilcesiz_yonetici_reddedilir() {
        kullanici(User.Role.ADMIN, null);
        oturumAc("ADMIN");

        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .withFailMessage("""
                        Yönetici ilçesiz kapsam alabildi. ADMIN kapıdan geçebiliyor
                        ama "ilçesiz kapsam" hiç kimsede oluşmamalı; aksi hâlde
                        panel sorgusu filtresiz koşar.""")
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Oturum yoksa reddedilir")
    void oturumsuz_reddedilir() {
        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Anonim oturum kapsam alamaz")
    void anonim_reddedilir() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("anahtar", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Jetonu geçerli ama kaydı silinmiş kullanıcı reddedilir")
    void kaydi_olmayan_reddedilir() {
        when(userRepository.findByEmail(EPOSTA)).thenReturn(Optional.empty());
        oturumAc("INSTITUTION");

        assertThatThrownBy(() -> kapsamCozucu.mevcutKapsam())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Rol JETONDAN değil VERİTABANINDAN okunuyor")
    void rol_veritabanindan_okunuyor() {
        // Yönetici hesabı kuruma yükselttiğinde kullanıcının elindeki jeton
        // hâlâ "USER" taşır. Kapsam jetona bağlansaydı, yükseltme sonrası
        // kullanıcı kapıdan geçse bile kapsam çözülemez ve sebebi görünmezdi.
        kullanici(User.Role.INSTITUTION, "Nilüfer");
        oturumAc("USER");   // jeton eski rolü taşıyor

        assertThat(kapsamCozucu.mevcutIlce()).isEqualTo("Nilüfer");
    }
}
