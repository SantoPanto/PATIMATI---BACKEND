package com.works.patimati.service;

import com.works.patimati.dto.ad.SharedAdDTO;
import com.works.patimati.dto.message.AdShareRequestDTO;
import com.works.patimati.dto.message.ChatRoomWithAdResponseDTO;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.MessageType;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessageServiceAdShareTest {

    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private AdRepository adRepository;
    private SimpMessagingTemplate messagingTemplate;
    private NotificationService notificationService;
    private MessageService messageService;

    private User userA;
    private User userB;
    private User userC;
    private Ad adOfUserB;
    private Ad ad2OfUserB;
    private Ad adOfUserC;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        userRepository = mock(UserRepository.class);
        adRepository = mock(AdRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        notificationService = mock(NotificationService.class);

        messageService = new MessageService(
                messageRepository,
                userRepository,
                adRepository,
                mock(MessageFraudFilterService.class),
                messagingTemplate,
                notificationService
        );

        userA = User.builder().uid(42L).email("userA@patimati.com").firstName("User").lastName("A").build();
        userB = User.builder().uid(73L).email("userB@patimati.com").firstName("User").lastName("B").build();
        userC = User.builder().uid(99L).email("userC@patimati.com").firstName("User").lastName("C").build();

        adOfUserB = Ad.builder()
                .id(154L)
                .title("Kayıp Tekir Kedi")
                .adType(Ad.AdType.LOST)
                .species(Species.CAT)
                .breed("Tekir")
                .photoUrls(List.of("https://s3.patimati.com/cat.jpg"))
                .user(userB)
                .active(true)
                .build();

        ad2OfUserB = Ad.builder()
                .id(205L)
                .title("Sahiplendirilecek Golden")
                .adType(Ad.AdType.ADOPTION)
                .species(Species.DOG)
                .breed("Golden Retriever")
                .photoUrls(List.of("https://s3.patimati.com/dog.jpg"))
                .user(userB)
                .active(true)
                .build();

        adOfUserC = Ad.builder()
                .id(319L)
                .title("Bulundu Poodle")
                .adType(Ad.AdType.FOUND)
                .species(Species.DOG)
                .breed("Poodle")
                .user(userC)
                .active(true)
                .build();

        when(userRepository.findByEmail("userA@patimati.com")).thenReturn(Optional.of(userA));
        when(userRepository.findByEmail("userB@patimati.com")).thenReturn(Optional.of(userB));
        when(userRepository.findByEmail("userC@patimati.com")).thenReturn(Optional.of(userC));

        when(userRepository.findById(42L)).thenReturn(Optional.of(userA));
        when(userRepository.findById(73L)).thenReturn(Optional.of(userB));
        when(userRepository.findById(99L)).thenReturn(Optional.of(userC));

        when(adRepository.findById(154L)).thenReturn(Optional.of(adOfUserB));
        when(adRepository.findById(205L)).thenReturn(Optional.of(ad2OfUserB));
        when(adRepository.findById(319L)).thenReturn(Optional.of(adOfUserC));
    }

    @Test
    @DisplayName("İlk sohbet - İki kullanıcı ilk kez konuşuyor: Yeni AD_SHARE mesajı kaydedilmeli ve oda dönülmeli")
    void shareAdAndGetRoom_FirstInteraction_ShouldSaveMessageAndReturnRoom() {
        AdShareRequestDTO request = new AdShareRequestDTO(73L, 154L);

        when(messageRepository.findTopBySender_UidAndRecipient_UidOrderByIdDesc(42L, 73L))
                .thenReturn(Optional.empty());

        Message savedMsg = Message.builder()
                .id(984L)
                .sender(userA)
                .recipient(userB)
                .content("İlan Paylaşıldı: Kayıp Tekir Kedi")
                .type(MessageType.AD_SHARE)
                .sharedAd(adOfUserB)
                .timestamp(Instant.now())
                .read(false)
                .build();

        when(messageRepository.save(any(Message.class))).thenReturn(savedMsg);

        // findChatHistory for createOrGetRoom
        when(messageRepository.findChatHistory(eq(42L), eq(73L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(savedMsg)));

        ChatRoomWithAdResponseDTO response = messageService.shareAdAndGetRoom("userA@patimati.com", request);

        assertThat(response).isNotNull();
        assertThat(response.room()).isNotNull();
        assertThat(response.room().partnerId()).isEqualTo(73L);
        assertThat(response.sharedMessage()).isNotNull();
        assertThat(response.sharedMessage().id()).isEqualTo(984L);
        assertThat(response.sharedMessage().type()).isEqualTo(MessageType.AD_SHARE);
        assertThat(response.sharedMessage().sharedAd()).isNotNull();
        assertThat(response.sharedMessage().sharedAd().id()).isEqualTo(154L);
        assertThat(response.sharedMessage().sharedAd().photoUrl()).isEqualTo("https://s3.patimati.com/cat.jpg");

        // Verify WebSocket broadcasting
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("userB@patimati.com"), eq("/queue/messages"), any(MessageResponse.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("userA@patimati.com"), eq("/queue/messages"), any(MessageResponse.class));
    }

    @Test
    @DisplayName("Mevcut oda yeniden kullanımı - Aynı kullanıcılar farklı bir ilan için mesajlaştığında aynı oda dönülmeli")
    void shareAdAndGetRoom_ExistingRoom_ShouldReuseSameRoom() {
        AdShareRequestDTO request = new AdShareRequestDTO(73L, 205L);

        Message existingMsg = Message.builder()
                .id(984L)
                .sender(userA)
                .recipient(userB)
                .content("İlan Paylaşıldı: Kayıp Tekir Kedi")
                .type(MessageType.AD_SHARE)
                .sharedAd(adOfUserB)
                .timestamp(Instant.now().minusSeconds(300))
                .read(true)
                .build();

        when(messageRepository.findTopBySender_UidAndRecipient_UidOrderByIdDesc(42L, 73L))
                .thenReturn(Optional.of(existingMsg));

        Message newMsg = Message.builder()
                .id(985L)
                .sender(userA)
                .recipient(userB)
                .content("İlan Paylaşıldı: Sahiplendirilecek Golden")
                .type(MessageType.AD_SHARE)
                .sharedAd(ad2OfUserB)
                .timestamp(Instant.now())
                .read(false)
                .build();

        when(messageRepository.save(any(Message.class))).thenReturn(newMsg);

        when(messageRepository.findChatHistory(eq(42L), eq(73L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newMsg, existingMsg)));

        ChatRoomWithAdResponseDTO response = messageService.shareAdAndGetRoom("userA@patimati.com", request);

        assertThat(response.room().partnerId()).isEqualTo(73L);
        assertThat(response.sharedMessage().id()).isEqualTo(985L);
        assertThat(response.sharedMessage().sharedAd().id()).isEqualTo(205L);
    }

    @Test
    @DisplayName("Güvenlik - Başka kullanıcının ilanı paylaşılmaya çalışılırsa reddedilmeli")
    void shareAdAndGetRoom_AdNotOwnedByTargetUser_ShouldThrowException() {
        // User A tries to share adOfUserC (owned by C) with User B
        AdShareRequestDTO request = new AdShareRequestDTO(73L, 319L);

        assertThatThrownBy(() -> messageService.shareAdAndGetRoom("userA@patimati.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Paylaşılmak istenen ilan hedef kullanıcıya ait değil.");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Güvenlik - Kullanıcı kendisi ile sohbet başlatamaz")
    void shareAdAndGetRoom_SelfChat_ShouldThrowException() {
        AdShareRequestDTO request = new AdShareRequestDTO(42L, 154L);

        assertThatThrownBy(() -> messageService.shareAdAndGetRoom("userA@patimati.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kullanıcı kendisi ile sohbet odası oluşturamaz.");
    }

    @Test
    @DisplayName("Çift tıklama koruması - 5 saniye içinde aynı ilanın tekrar paylaşılması mükerrer mesaj oluşturmamalı")
    void shareAdAndGetRoom_DoubleClick_ShouldNotCreateDuplicateMessage() {
        AdShareRequestDTO request = new AdShareRequestDTO(73L, 154L);

        Message recentMsg = Message.builder()
                .id(984L)
                .sender(userA)
                .recipient(userB)
                .content("İlan Paylaşıldı: Kayıp Tekir Kedi")
                .type(MessageType.AD_SHARE)
                .sharedAd(adOfUserB)
                .timestamp(Instant.now().minusSeconds(1)) // 1 second ago
                .read(false)
                .build();

        when(messageRepository.findTopBySender_UidAndRecipient_UidOrderByIdDesc(42L, 73L))
                .thenReturn(Optional.of(recentMsg));

        when(messageRepository.findChatHistory(eq(42L), eq(73L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(recentMsg)));

        ChatRoomWithAdResponseDTO response = messageService.shareAdAndGetRoom("userA@patimati.com", request);

        assertThat(response.sharedMessage().id()).isEqualTo(984L);
        // Verify save was NOT called again
        verify(messageRepository, never()).save(any());
    }
}
