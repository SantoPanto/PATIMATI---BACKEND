package com.works.patimati.service;

import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.ChatRoomResponseDTO;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final MessageFraudFilterService fraudFilterService;
    private final SimpMessagingTemplate messagingTemplate;

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

        // Anlık İletim (Broadcast) - Hem alıcının hem de gönderenin özel WebSocket kuyruğuna iletiliyor
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.recipientId()),
                "/queue/messages",
                response
        );
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.senderId()),
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
                    unreadCount
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
                    unreadCount
            );
        }

        return new ChatRoomResponseDTO(
                null,
                partner.getUid(),
                partnerName,
                null,
                null,
                null,
                0
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

        // Son mesajları almak için DESC sorgulanır
        Pageable descPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "timestamp", "id"));
        Page<Message> messagePage = messageRepository.findChatHistory(currentUser.getUid(), otherUserId, descPageable);

        // Kronolojik sırayla (ASC - Eskiden Yeniye, yeni mesaj en altta) sunmak için liste çevrilir
        List<MessageResponse> list = new ArrayList<>(messagePage.getContent().stream()
                .map(message -> mapToResponse(message, currentUser))
                .toList());
        Collections.reverse(list);

        return new PageImpl<>(list, pageable, messagePage.getTotalElements());
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
                message.isRead()
        );
    }
}