package com.works.patimati.municipality;

import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Belediye panelinin KAPSAMINI çözer: bu isteği yapan kurum hangi ilçeyi görebilir?
 *
 * <p><b>Neden bu sınıf var — modülün tek güvenlik taşıyıcısı:</b> ilçe bilgisi
 * istekten (parametre, başlık, gövde) ASLA alınmaz; oturumdaki kullanıcıdan
 * sunucuda türetilir. Aksi hâlde herhangi bir belediye hesabı
 * {@code ?ilce=Çankaya} yazarak başka belediyenin verisini okur — ve bu, uç
 * "yalnız INSTITUTION'a açık" olsa bile gerçekleşir. Yetki kontrolü "kim
 * girebilir"i çözer, kapsam çözücü "ne görebilir"i.
 *
 * <p><b>A ve C2 uçları bunu çağırmak zorunda:</b> panel sorgusu da ihbar
 * kuyruğu da kendi ilçesini kendisi belirlememeli. Kullanım:
 * <pre>
 *   var kapsam = municipalityScopeService.mevcutKapsam();
 *   ... WHERE LOWER(a.district) = LOWER(:ilce) ...   // kapsam.ilce()
 * </pre>
 *
 * <p><b>Kıyas neden SQL'de ve iki tarafa da LOWER ile:</b> {@code ads.district}
 * değerlerini ReverseGeocodingService, Nominatim'den {@code accept-language=tr}
 * ile alıyor — yani "Nilüfer" gibi Türkçe yazımla. Kuruma ilçe atanırken elle
 * yazılan değer büyük/küçük harfte ayrışabilir. Küçültmeyi Java'da yapmak yeni
 * bir tuzak açardı: Java'nın Türkçe yerelinde {@code "I".toLowerCase()} → "ı",
 * PostgreSQL'in {@code lower()}'ı ise sunucunun collation'ına bağlı. İki taraf
 * ayrı ayrı küçültülürse aynı ilçe eşleşmeyebilir. Bu yüzden kapsam çözücü HAM
 * (yalnız kırpılmış) değeri verir, kıyası tek motorda — veritabanında — yapın.
 *
 * <p><b>Boş kapsam "hepsini göster" DEĞİLDİR:</b> ilçe çözülemezse istek
 * reddedilir. Sessizce filtresiz sorguya düşmek, tek bir yanlış yapılandırılmış
 * kurum hesabına ülkenin tamamını açardı.
 */
@Service
public class MunicipalityScopeService {

    private final UserRepository userRepository;

    public MunicipalityScopeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Bir kurumun görebildiği alan.
     *
     * @param kurumKullaniciId kurum hesabının users.uid'i (kuyruk atamalarında lazım)
     * @param kurumAdi         panel başlığında gösterilecek ad; null olabilir
     * @param il               kurumun ili; null olabilir (kapsamı ilçe belirler)
     * @param ilce             KAPSAM — sorguların filtreleyeceği ilçe, asla boş değil
     */
    public record BelediyeKapsami(Long kurumKullaniciId, String kurumAdi, String il, String ilce) {
    }

    /**
     * Oturumdaki kullanıcıdan kapsamı türetir.
     *
     * @throws AccessDeniedException oturum yoksa, kullanıcı bulunamazsa, rolü
     *                               kurum/yönetici değilse ya da ilçesi atanmamışsa
     */
    @Transactional(readOnly = true)
    public BelediyeKapsami mevcutKapsam() {
        User kullanici = oturumSahibi();

        // ADMIN de kabul ediliyor: prova ve sunumda paneli kurum hesabı açmadan
        // görebilmek gerekiyor (SecurityConfig'de de böyle yazılı). Ama muafiyet
        // DEĞİL — yöneticinin de institution_district'i dolu olmalı, yoksa
        // aşağıdaki kontrol onu da reddeder. Yani "ilçesiz kapsam" hiç kimsede
        // oluşmuyor.
        User.Role rol = kullanici.getRole();
        if (rol != User.Role.INSTITUTION && rol != User.Role.ADMIN) {
            throw new AccessDeniedException(
                    "Bu uç yalnızca kurum (belediye) hesaplarına açıktır.");
        }

        String ilce = kirp(kullanici.getInstitutionDistrict());
        if (ilce == null) {
            throw new AccessDeniedException(
                    "Hesabınıza ilçe atanmamış. Belediye panelini kullanabilmek için "
                            + "yöneticiden hesabınıza il/ilçe tanımlanmasını isteyin.");
        }

        return new BelediyeKapsami(
                kullanici.getUid(),
                kirp(kullanici.getInstitutionName()),
                kirp(kullanici.getInstitutionCity()),
                ilce);
    }

    /** Yalnız ilçe gerekiyorsa kısayol. Aynı kontrollerden geçer. */
    @Transactional(readOnly = true)
    public String mevcutIlce() {
        return mevcutKapsam().ilce();
    }

    private User oturumSahibi() {
        Authentication kimlik = SecurityContextHolder.getContext().getAuthentication();
        if (kimlik == null || !kimlik.isAuthenticated()
                || "anonymousUser".equals(kimlik.getPrincipal())) {
            throw new AccessDeniedException("Oturum bulunamadı.");
        }

        // JwtAuthFilter principal olarak e-postayı koyuyor (UserDetails değil).
        Optional<User> kullanici = userRepository.findByEmail(kimlik.getName());
        if (kullanici.isEmpty()) {
            // Jeton geçerli ama kullanıcı silinmiş: 403, çünkü kimlik doğru,
            // yetkilendirilecek kayıt yok.
            throw new AccessDeniedException("Oturum sahibi kullanıcı bulunamadı.");
        }

        // Rol JETONDAN değil VERİTABANINDAN okunuyor. JwtAuthFilter yetkiyi
        // jetondaki claim'den kuruyor; yönetici bir hesabı kuruma yükselttiğinde
        // o hesabın elindeki eski jeton hâlâ "USER" taşır. Kapsamı jetona
        // bağlasaydık, yükseltme sonrası ilçe çözülemez ve kullanıcı sebebini
        // anlamadan reddedilirdi. (Yeniden giriş yine de gerekiyor: SecurityConfig
        // kapıyı jetondaki role bakarak açıyor.)
        return kullanici.get();
    }

    private static String kirp(String deger) {
        if (deger == null) {
            return null;
        }
        String kirpilmis = deger.trim();
        return kirpilmis.isEmpty() ? null : kirpilmis;
    }
}
