package com.works.patimati.security;

import com.works.patimati.dto.message.ChatRoomResponseDTO;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.MessageFraudFilterService;
import com.works.patimati.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Sohbet odası açma ucu, çağıranın karşı tarafla GERÇEK ilişkisi olmasını istemeli.
 *
 * <p>NEDEN VAR: bu uçta hiçbir yetki kontrolü yoktu. Giriş yapmış sıradan bir
 * kullanıcı kimlik numaralarını 1'den başlayıp sırayla deneyerek sistemdeki
 * herkesin adını soyadını dökebiliyordu — yöneticininki dâhil. Ölçüldü:
 * {@code POST /api/messages/rooms/{1..5}} hepsi 200 ve gövdede
 * {@code partnerName}; {@code uid 999} ise 404. Yani uç aynı zamanda bir
 * VARLIK KÂHİNİYDİ: 200 "bu kullanıcı var", 404 "yok" demekti ve kimlikler
 * ardışık olduğu için toplam kullanıcı sayısı bile öğrenilebiliyordu.
 *
 * <p>Bu yüzden testlerin en kritiği {@link #iliskisiz_kullanici_ile_var_olmayan_kullanici_ayirt_edilemiyor()}:
 * yalnızca "isim dönmesin" demek YETMEZ. Farklı hata mesajı ya da farklı HTTP
 * kodu vermek sızıntıyı sürdürürdü — 403 "yetkin yok" cümlesi bile o kişinin
 * VAR olduğunu söyler. İki durumun cevabı ayırt edilemez olmalı.
 */
class MesajOdasiIliskiIstiyorTest {

    private static final String CAGIRAN_EPOSTA = "cagiran@patimati.local";
    private static final long CAGIRAN_UID = 1L;
    private static final long HEDEF_UID = 42L;
    private static final long OLMAYAN_UID = 999L;

    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private AdRepository adRepository;
    private MessageService messageService;

    private User cagiran;
    private User hedef;

    @BeforeEach
    void hazirla() {
        messageRepository = mock(MessageRepository.class);
        userRepository = mock(UserRepository.class);
        adRepository = mock(AdRepository.class);

        messageService = new MessageService(
                messageRepository,
                userRepository,
                adRepository,
                mock(MessageFraudFilterService.class),
                mock(SimpMessagingTemplate.class)
        );

        cagiran = User.builder().uid(CAGIRAN_UID).email(CAGIRAN_EPOSTA).build();
        hedef = User.builder()
                .uid(HEDEF_UID)
                .email("hedef@patimati.local")
                .firstName("Admin")
                .lastName("Local")
                .build();

        when(userRepository.findByEmail(CAGIRAN_EPOSTA)).thenReturn(Optional.of(cagiran));
        when(userRepository.findById(HEDEF_UID)).thenReturn(Optional.of(hedef));
        when(userRepository.findById(OLMAYAN_UID)).thenReturn(Optional.empty());

        // Varsayılan: hiç yazışma yok, hedefin halka açık ilanı yok -> ilişki yok.
        when(messageRepository.findChatHistory(anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(adRepository.existsByUser_UidAndActiveTrueAndSuspendedFalse(anyLong()))
                .thenReturn(false);
    }

    @Test
    void iliskisiz_kullanici_ile_var_olmayan_kullanici_ayirt_edilemiyor() {
        Throwable varAmaIliskisiz = hataYakala(HEDEF_UID);
        Throwable hicYok = hataYakala(OLMAYAN_UID);

        assertThat(varAmaIliskisiz)
                .as("ilişkisi olmayan VAR OLAN kullanıcı için oda açılmamalı")
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(hicYok)
                .as("var olmayan kullanıcı için de aynı tür hata dönmeli")
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(varAmaIliskisiz.getMessage())
                .as("""
                        İki cevap BİREBİR aynı olmalı. Farklıysa uç varlık kâhini olmaya \
                        devam eder: saldırgan mesajlara bakarak hangi kimliğin gerçek \
                        kullanıcıya ait olduğunu anlar.""")
                .isEqualTo(hicYok.getMessage());

        assertThat(varAmaIliskisiz.getMessage())
                .as("hata mesajı hedefin adını/soyadını ele vermemeli")
                .doesNotContain("Admin")
                .doesNotContain("Local")
                .doesNotContain(String.valueOf(HEDEF_UID));
    }

    @Test
    void halka_acik_ilani_olan_kullaniciyla_oda_acilabiliyor() {
        when(adRepository.existsByUser_UidAndActiveTrueAndSuspendedFalse(HEDEF_UID)).thenReturn(true);

        ChatRoomResponseDTO oda = messageService.createOrGetRoom(CAGIRAN_EPOSTA, HEDEF_UID);

        assertThat(oda.partnerName())
                .as("""
                        İlan sahibine mesaj atmak ÜRÜNÜN ASIL AKIŞI; kapatılmamalı. \
                        Bu isim zaten AdResponse.ownerDisplayName ile herkese görünüyor \
                        (o alan da firstName + " " + lastName üretiyor), yani yeni bir \
                        bilgi sızmıyor.""")
                .isEqualTo("Admin Local");
        assertThat(oda.partnerId()).isEqualTo(HEDEF_UID);
    }

    @Test
    void mevcut_yazismasi_olan_kullaniciyla_oda_acilabiliyor() {
        Message sonMesaj = Message.builder()
                .id(7L)
                .sender(hedef)
                .recipient(cagiran)
                .content("merhaba")
                .timestamp(Instant.parse("2026-08-19T10:00:00Z"))
                .read(false)
                .build();
        when(messageRepository.findChatHistory(eq(CAGIRAN_UID), eq(HEDEF_UID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sonMesaj)));

        ChatRoomResponseDTO oda = messageService.createOrGetRoom(CAGIRAN_EPOSTA, HEDEF_UID);

        assertThat(oda.partnerName())
                .as("aralarında yazışma varsa oda zaten meşrudur, ilan şartı aranmaz")
                .isEqualTo("Admin Local");
        assertThat(oda.lastMessage()).isEqualTo("merhaba");
    }

    @Test
    void askidaki_ilan_yetki_vermiyor() {
        // existsBy...ActiveTrueAndSuspendedFalse false döndüğü sürece (varsayılan)
        // ilişki kurulmamalı: askıya alınmış/kapatılmış ilan halka görünmüyor,
        // dolayısıyla sahibinin adı da görünmüyor.
        assertThatThrownBy(() -> messageService.createOrGetRoom(CAGIRAN_EPOSTA, HEDEF_UID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void kendisiyle_oda_acamiyor() {
        assertThatThrownBy(() -> messageService.createOrGetRoom(CAGIRAN_EPOSTA, CAGIRAN_UID))
                .as("""
                        Kendi kendine oda ayrı bir hata vermeye devam edebilir: çağıran \
                        zaten var olduğunu biliyor, burada sızdırılacak bir şey yok.""")
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Throwable hataYakala(long partnerId) {
        try {
            messageService.createOrGetRoom(CAGIRAN_EPOSTA, partnerId);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
}
