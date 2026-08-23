package com.works.patimati.service;

import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "Mesajlar canlı gelmiyor, F5 atmak gerekiyor" (23.08 saha bildirimi).
 *
 * <p><b>Ölçülen arıza (yerel, iki gerçek STOMP istemcisiyle):</b> gönderilen
 * mesaj veritabanına YAZILIYOR (REST geçmişinde görünüyor) ama alıcının
 * {@code /user/queue/messages} aboneliğine HİÇBİR ŞEY gelmiyordu.
 *
 * <p><b>Kök sebep:</b> Spring, {@code /user/{ad}/queue/...} hedefini oturumun
 * Principal'inin {@code getName()} değeriyle çözer.
 * {@code WebSocketChannelInterceptor} Principal'i KULLANICININ E-POSTASI
 * olarak kuruyor, oysa {@code sendMessage} hedefi
 * {@code String.valueOf(recipientId)} ile — yani SAYIYLA — veriyordu.
 * "91" adında bir Principal hiç var olmadığı için çerçeve sessizce düşüyordu.
 *
 * <p><b>Neden bu katmanda ölçülüyor:</b> arıza tip sisteminin göremeyeceği bir
 * DEĞER uyuşmazlığı — iki tarafta da {@code String}. Bekçi, hedefin sayıya
 * geri dönmesini yakalar.
 */
class MesajCanliTeslimatTest {

    private static final String GONDERICI_EPOSTA = "gonderen@ornek.com";
    private static final String ALICI_EPOSTA = "alici@ornek.com";

    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private SimpMessagingTemplate messagingTemplate;
    private MessageService messageService;

    private User gonderici;
    private User alici;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        userRepository = mock(UserRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);

        MessageFraudFilterService suzgec = mock(MessageFraudFilterService.class);
        when(suzgec.filterContent(anyString())).thenAnswer(c -> c.getArgument(0));

        messageService = new MessageService(
                messageRepository,
                userRepository,
                mock(AdRepository.class),
                suzgec,
                messagingTemplate,
                mock(NotificationService.class)
        );

        gonderici = User.builder().uid(90L).email(GONDERICI_EPOSTA)
                .firstName("Deneme").lastName("Sahip").build();
        alici = User.builder().uid(91L).email(ALICI_EPOSTA)
                .firstName("Deneme").lastName("Abone").build();

        when(userRepository.findByEmail(GONDERICI_EPOSTA)).thenReturn(Optional.of(gonderici));
        when(userRepository.findById(91L)).thenReturn(Optional.of(alici));

        when(messageRepository.save(any(Message.class))).thenAnswer(c -> {
            Message m = c.getArgument(0);
            m.setId(1000L);
            if (m.getTimestamp() == null) {
                m.setTimestamp(Instant.now());
            }
            return m;
        });
    }

    @Test
    @DisplayName("canlı mesaj hedefi E-POSTA ile verilir (sayısal id ile DEĞİL)")
    void canliMesajHedefiEpostaOlmali() {
        messageService.sendMessage(GONDERICI_EPOSTA, new MessageSendRequest(91L, "merhaba"));

        ArgumentCaptor<String> kullanici = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                kullanici.capture(), eq("/queue/messages"), any(Object.class));

        List<String> hedefler = kullanici.getAllValues();

        // Alıcı ve gönderici, ikisi de kendi oturum kimlikleriyle (e-posta).
        assertThat(hedefler).containsExactlyInAnyOrder(ALICI_EPOSTA, GONDERICI_EPOSTA);

        // Sayısal kimlik geri gelirse teslimat sessizce düşer — asıl arıza buydu.
        assertThat(hedefler).noneMatch(hedef -> hedef.chars().allMatch(Character::isDigit));
        assertThat(hedefler).doesNotContain("91", "90");
    }

    @Test
    @DisplayName("hedef, WebSocket Principal'inin adıyla AYNI alandan gelir")
    void hedefPrincipalAdiylaAyniAlandanGelir() {
        messageService.sendMessage(GONDERICI_EPOSTA, new MessageSendRequest(91L, "ikinci"));

        ArgumentCaptor<String> kullanici = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(2)).convertAndSendToUser(
                kullanici.capture(), anyString(), any(Object.class));

        /*
         * WebSocketChannelInterceptor Principal'i jwtService.extractEmail(token)
         * ile, yani KULLANICI.getEmail() ile kuruyor. Bekçi bu iki alanın aynı
         * kalmasını kilitliyor: biri değişirse (ör. Principal'e uid konursa)
         * bu iddia kırılır ve teslimat sessizce ölmez.
         */
        assertThat(kullanici.getAllValues())
                .containsExactlyInAnyOrder(alici.getEmail(), gonderici.getEmail());
    }
}
