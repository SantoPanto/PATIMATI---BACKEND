package com.works.patimati.service;

import com.works.patimati.dto.ad.SharedAdDTO;
import com.works.patimati.dto.message.AdShareRequestDTO;
import com.works.patimati.dto.message.ChatRoomWithAdResponseDTO;
import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.ChatRoomResponseDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.MessageType;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final MessageFraudFilterService fraudFilterService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    /**
     * İlişkisiz kullanıcı ile VAR OLMAYAN kullanıcı için TEK TİP cevap.
     * <p>
     * Ayrı mesaj/kod kullanmak varlık kâhini üretir: 200 "var", 404 "yok" demek
     * olur ve kimlikler ardışık olduğu için toplam kullanıcı sayısı bile
     * öğrenilebilir. 403 de aynı sızıntıyı yapar — "yetkin yok" cümlesi o
     * kişinin VAR olduğunu söyler. Bu yüzden iki durum da buradan geçiyor.
     */
    private static final String ODA_ACILAMAZ = "Kullanıcı bulunamadı.";

    @Transactional
    public MessageResponse sendMessage(String senderEmail, MessageSendRequest request) {
        User sender = findUserByEmail(senderEmail);
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı kullanıcı bulunamadı: " + request.recipientId()));

        if (sender.getUid().equals(recipient.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendisine mesaj gönderemez.");
        }

        String filteredContent = fraudFilterService.filterContent(request.content());
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(filteredContent)
                .timestamp(Instant.now())
                .read(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        MessageResponse response = mapToResponse(savedMessage, sender);

        log.info("[WS SEND] [WS TARGET USER] [WS DESTINATION] messageId={} saved to DB. convertAndSendToUser destination=/queue/messages, recipientEmail={}, senderEmail={}",
                savedMessage.getId(), recipient.getEmail(), sender.getEmail());

        // Anlık İletim (Broadcast) - Hem alıcının hem de gönderenin özel WebSocket kuyruğuna iletiliyor
        notificationService.createOrStackMessageNotification(
                recipient,
                sender,
                savedMessage.getId()
        );

        /*
         * convertAndSendToUser'ın ilk parametresi, WebSocketChannelInterceptor'ın
         * STOMP CONNECT sırasında Principal'e verdiği İSİMLE (getName()) birebir
         * eşleşmelidir -- Spring bu ada göre kullanıcının gerçek oturum kuyruğunu
         * bulur. O principal e-posta ile kuruluyor (bkz. WebSocketChannelInterceptor
         * satır ~108: "Principal ismimiz kullanıcının e-posta adresidir"), sayısal
         * uid ile DEĞİL. Burada uid kullanmak mesajın hiçbir zaman hedef oturuma
         * ulaşmamasına yol açardı -- Spring, eşleşen kullanıcı bulamayınca mesajı
         * sessizce yok sayar.
         */
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/messages",
                response
        );
        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/messages",
                response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getUserChatRooms(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        List<Message> recentMessages = messageRepository.findRecentMessagesPerConversation(currentUser.getUid());

        return recentMessages.stream().map(message -> {
            boolean isSenderCurrent = currentUser.getUid().equals(message.getSender().getUid());
            User partner = isSenderCurrent ? message.getRecipient() : message.getSender();
            String partnerName = partner.getFirstName() + " " + partner.getLastName();
            long unreadCount = messageRepository.countBySender_UidAndRecipient_UidAndReadFalse(partner.getUid(), currentUser.getUid());

            return new ChatRoomResponseDTO(
                    message.getId(),
                    partner.getUid(),
                    partnerName,
                    null,
                    message.getContent(),
                    message.getTimestamp(),
                    unreadCount,
                    partner.getRole()
            );
        }).toList();
    }

    /**
     * Sohbet odasını açar ya da mevcut odayı döndürür.
     * <p>
     * <b>YETKİ KURALI (19.08.2026'da eklendi):</b> çağıranın karşı tarafla
     * GERÇEK bir ilişkisi olmalı — ya aralarında yazışma var, ya karşı tarafın
     * halka açık bir ilanı var. Öncesinde hiçbir kontrol yoktu: giriş yapmış
     * herhangi bir kullanıcı 1'den başlayıp kimlik numaralarını deneyerek
     * sistemdeki HERKESİN adını soyadını dökebiliyordu — yöneticininki dâhil.
     * Ölçüldü: {@code POST /api/messages/rooms/{1..5}} hepsi 200 ve gövdede
     * {@code partnerName} dönüyordu; {@code uid 999} ise 404 veriyordu, yani uç
     * aynı zamanda bir varlık kâhiniydi.
     * <p>
     * "Halka açık ilanı var" ölçütü bilerek seçildi: öyle bir kullanıcının adı
     * {@code AdResponse.ownerDisplayName} ile zaten herkese görünüyor ve o alan
     * da {@code firstName + " " + lastName} üretiyor — yani birebir aynı bilgi.
     * Dolayısıyla bu izin yeni bir şey sızdırmıyor, sadece var olan akışı
     * (ilan sahibine mesaj atmak) çalışır tutuyor.
     * <p>
     * <b>YÖNETİCİ MUAFİYETİ:</b> çağıran {@code ADMIN} ise ilişki şartı
     * aranmaz. Yöneticinin işi tam olarak <i>kendisiyle hiçbir ilişkisi
     * olmayan</i> kullanıcıya ulaşmaktır: şikayet ekranındaki "Kullanıcıyla
     * Sohbet Et" düğmesi şikayet edilen kişiye gider ve o kişinin halka açık
     * ilanı olmayabilir (şikayet üzerine askıya alınmış olabilir — ki
     * {@code askidaki_ilan_yetki_vermiyor} bunu bilerek reddediyor). Muafiyet
     * olmadan düğme 404'ten kurtulamaz, yalnızca 404'ün kaynağı değişirdi.
     * <p>
     * Muafiyet yeni bir sızıntı açmıyor: yönetici {@code /api/auth/userlist}
     * ile zaten tüm kullanıcıları görebiliyor (B-3 kapsamında bilerek
     * yöneticiye kilitlendi). Varlık kâhini kaygısı sıradan kullanıcı içindi.
     * <b>Var olmayan</b> kimlik yöneticiye de aynı tek tip cevabı verir —
     * muafiyet ilişki şartını kaldırır, kullanıcının var olma şartını değil.
     */
    @Transactional
    public ChatRoomResponseDTO createOrGetRoom(String currentUserEmail, Long partnerId) {
        User currentUser = findUserByEmail(currentUserEmail);

        // Kendi kendine oda: çağıran zaten var olduğunu biliyor, sızıntı yok.
        // Bu yüzden tek tip cevaba karışmıyor, kendi hatasını vermeye devam ediyor.
        if (currentUser.getUid().equals(partnerId)) {
            throw new IllegalArgumentException("Kullanıcı kendisi ile sohbet odası oluşturamaz.");
        }

        User partner = userRepository.findById(partnerId).orElse(null);

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> history = partner == null
                ? Page.<Message>empty(pageable)
                : messageRepository.findChatHistory(currentUser.getUid(), partner.getUid(), pageable);

        boolean cagiranYonetici = currentUser.getRole() == User.Role.ADMIN;

        boolean iliskiVar = partner != null
                && (cagiranYonetici
                    || history.hasContent()
                    || adRepository.existsByUser_UidAndActiveTrueAndSuspendedFalse(partner.getUid()));

        // Kullanıcı yoksa da, varsa ama ilişki yoksa da AYNI cevap. Ayırt
        // edilebilir olsalardı uç yine varlık kâhini olurdu.
        if (!iliskiVar) {
            throw new ResourceNotFoundException(ODA_ACILAMAZ);
        }

        String partnerName = partner.getFirstName() + " " + partner.getLastName();

        if (history.hasContent()) {
            Message lastMessage = history.getContent().get(0);
            long unreadCount = messageRepository.countBySender_UidAndRecipient_UidAndReadFalse(partner.getUid(), currentUser.getUid());
            return new ChatRoomResponseDTO(
                    lastMessage.getId(),
                    partner.getUid(),
                    partnerName,
                    null,
                    lastMessage.getContent(),
                    lastMessage.getTimestamp(),
                    unreadCount,
                    partner.getRole()
            );
        }

        return new ChatRoomResponseDTO(
                null,
                partner.getUid(),
                partnerName,
                null,
                null,
                null,
                0,
                partner.getRole()
        );
    }

    @Transactional
    public ChatRoomResponseDTO createChatRoom(String currentUserEmail, Long partnerId) {
        return createOrGetRoom(currentUserEmail, partnerId);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getChatHistory(String currentUserEmail, Long otherUserId, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);

        if (currentUser.getUid().equals(otherUserId)) {
            throw new IllegalArgumentException("Kullanıcı kendisi ile sohbet geçmişi sorgulayamaz.");
        }

        Pageable descPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.desc("timestamp"),
                        Sort.Order.desc("id")
                )
        );
        Page<Message> messagePage = messageRepository.findChatHistory(currentUser.getUid(), otherUserId, descPageable);

        return messagePage.map(message -> mapToResponse(message, currentUser));
    }

    @Transactional
    public void markAsRead(String currentUserEmail, Long messageId) {
        User currentUser = findUserByEmail(currentUserEmail);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesaj bulunamadı: " + messageId));
        if (message.getRecipient().getUid().equals(currentUser.getUid())) {
            message.setRead(true);
            messageRepository.save(message);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return messageRepository.countByRecipient_UidAndReadFalse(currentUser.getUid());
    }

    /**
     * İlan bağlamında sohbet odası açar / mevcut odayı getirir ve AD_SHARE mesajını kaydeder/iletir.
     */
    @Transactional
    public ChatRoomWithAdResponseDTO shareAdAndGetRoom(String currentUserEmail, AdShareRequestDTO request) {
        User sender = findUserByEmail(currentUserEmail);
        User recipient = userRepository.findById(request.targetUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ODA_ACILAMAZ));

        if (sender.getUid().equals(recipient.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendisi ile sohbet odası oluşturamaz.");
        }

        Ad ad = adRepository.findById(request.adId())
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: " + request.adId()));

        if (ad.getUser() == null || !ad.getUser().getUid().equals(recipient.getUid())) {
            throw new IllegalArgumentException("Paylaşılmak istenen ilan hedef kullanıcıya ait değil.");
        }

        // Çift tıklama / Mükerrer istek koruması: Son 5 saniye içinde aynı göndericiden aynı alıcıya aynı ilan paylaşıldıysa mesajı tekrar oluşturma
        Optional<Message> lastSentMessage = messageRepository.findTopBySender_UidAndRecipient_UidOrderByIdDesc(sender.getUid(), recipient.getUid());
        Message savedMessage;
        if (lastSentMessage.isPresent()
                && lastSentMessage.get().getType() == MessageType.AD_SHARE
                && lastSentMessage.get().getSharedAd() != null
                && lastSentMessage.get().getSharedAd().getId().equals(ad.getId())
                && lastSentMessage.get().getTimestamp() != null
                && Duration.between(lastSentMessage.get().getTimestamp(), Instant.now()).getSeconds() < 5) {
            savedMessage = lastSentMessage.get();
        } else {
            String content = "İlan Paylaşıldı: " + ad.getTitle();
            Message message = Message.builder()
                    .sender(sender)
                    .recipient(recipient)
                    .content(content)
                    .type(MessageType.AD_SHARE)
                    .sharedAd(ad)
                    .timestamp(Instant.now())
                    .read(false)
                    .build();

            savedMessage = messageRepository.save(message);

            MessageResponse response = mapToResponse(savedMessage, sender);

            log.info("[WS SEND] [AD_SHARE] messageId={} shared adId={} saved to DB.", savedMessage.getId(), ad.getId());

            notificationService.createOrStackMessageNotification(
                    recipient,
                    sender,
                    savedMessage.getId()
            );

            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),
                    "/queue/messages",
                    response
            );
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(),
                    "/queue/messages",
                    response
            );
        }

        ChatRoomResponseDTO room = createOrGetRoom(currentUserEmail, recipient.getUid());
        MessageResponse sharedMessageResponse = mapToResponse(savedMessage, sender);

        return new ChatRoomWithAdResponseDTO(room, sharedMessageResponse);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private MessageResponse mapToResponse(Message message) {
        return mapToResponse(message, message.getSender());
    }

    private MessageResponse mapToResponse(Message message, User currentUser) {
        boolean isSenderCurrent = currentUser != null && currentUser.getUid().equals(message.getSender().getUid());
        User partner = isSenderCurrent ? message.getRecipient() : message.getSender();
        String partnerName = partner.getFirstName() + " " + partner.getLastName();

        SharedAdDTO sharedAdDTO = (message.getType() == MessageType.AD_SHARE && message.getSharedAd() != null)
                ? SharedAdDTO.fromEntity(message.getSharedAd())
                : null;

        return new MessageResponse(
                message.getId(),
                message.getSender().getUid(),
                message.getSender().getFirstName() + " " + message.getSender().getLastName(),
                message.getRecipient().getUid(),
                message.getRecipient().getFirstName() + " " + message.getRecipient().getLastName(),
                partner.getUid(),
                partnerName,
                null,
                message.getContent(),
                message.getTimestamp(),
                message.isRead(),
                partner.getRole(),
                message.getType() != null ? message.getType() : MessageType.TEXT,
                sharedAdDTO
        );
    }
}
