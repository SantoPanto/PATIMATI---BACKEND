package com.works.patimati.service;

import com.works.patimati.dto.pet.PetResponse;
import com.works.patimati.dto.vetcustomer.VetCustomerRequestResponse;
import com.works.patimati.dto.vetcustomer.VetCustomerResponse;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetCustomerRequest;
import com.works.patimati.entity.enums.VetCustomerRequestStatus;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetCustomerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kullanıcı ↔ veteriner "müşteri isteği" akışı: gönder → vet kabul/red eder
 * → kabul edilen satırlar ("Müşterilerim") üzerinden vet, müşterisinin
 * hayvanlarını görebilir. Talep/kabul/red deseni {@code PotentialMatchService}
 * ile AYNI ilke (sahiplik kontrolü + durum geçişi + bildirim), tek satırlık
 * basitleştirilmiş hali.
 */
@Service
@RequiredArgsConstructor
public class VetCustomerService {

    private final VetCustomerRequestRepository vetCustomerRequestRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PetService petService;
    private final NotificationService notificationService;

    @Transactional
    public VetCustomerRequestResponse sendRequest(String requesterEmail, Long vetId) {
        User requester = findUserByEmail(requesterEmail);
        User vet = userRepository.findById(vetId)
                .orElseThrow(() -> new ResourceNotFoundException("Veteriner bulunamadı: " + vetId));

        if (vet.getRole() != User.Role.VET) {
            throw new BusinessException("Bu kullanıcı bir veteriner değil");
        }
        if (requester.getUid().equals(vet.getUid())) {
            throw new BusinessException("Kendinize müşteri isteği gönderemezsiniz");
        }

        Optional<VetCustomerRequest> existing =
                vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(requester.getUid(), vet.getUid());

        VetCustomerRequest request;
        if (existing.isPresent()) {
            request = existing.get();
            if (request.getStatus() != VetCustomerRequestStatus.REJECTED) {
                throw new BusinessException(
                        request.getStatus() == VetCustomerRequestStatus.ACCEPTED
                                ? "Bu veterinerin zaten müşterisisiniz"
                                : "Bu veterinere zaten bir isteğiniz var"
                );
            }
            request.setStatus(VetCustomerRequestStatus.PENDING);
            request.setDecidedAt(null);
        } else {
            request = VetCustomerRequest.builder()
                    .requester(requester)
                    .vet(vet)
                    .status(VetCustomerRequestStatus.PENDING)
                    .build();
        }

        VetCustomerRequest saved = vetCustomerRequestRepository.save(request);

        notificationService.createAndSend(
                vet,
                "Yeni müşteri isteği",
                requester.getFirstName() + " " + requester.getLastName() + " sizi veterineri olarak eklemek istiyor.",
                "VET_CUSTOMER_REQUEST",
                Map.of("requesterId", String.valueOf(requester.getUid()))
        );

        return toRequestResponse(saved);
    }

    /** İstek hiç gönderilmemişse 404 (frontend bunu "istek yok" olarak yorumlar). */
    @Transactional(readOnly = true)
    public VetCustomerRequestResponse getMyRequestStatus(String requesterEmail, Long vetId) {
        User requester = findUserByEmail(requesterEmail);
        return vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(requester.getUid(), vetId)
                .map(this::toRequestResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Bu veterinere gönderilmiş bir isteğiniz yok"));
    }

    @Transactional(readOnly = true)
    public List<VetCustomerRequestResponse> listIncomingRequests(String vetEmail, Pageable pageable) {
        User vet = findUserByEmail(vetEmail);
        return vetCustomerRequestRepository
                .findByVet_UidAndStatus(vet.getUid(), VetCustomerRequestStatus.PENDING, pageable)
                .map(this::toRequestResponse)
                .getContent();
    }

    @Transactional
    public VetCustomerRequestResponse accept(String vetEmail, Long requestId) {
        VetCustomerRequest request = findOwnedRequest(vetEmail, requestId);
        if (request.getStatus() != VetCustomerRequestStatus.PENDING) {
            throw new BusinessException("Bu istek zaten karara bağlanmış");
        }
        request.setStatus(VetCustomerRequestStatus.ACCEPTED);
        request.setDecidedAt(OffsetDateTime.now());
        VetCustomerRequest saved = vetCustomerRequestRepository.save(request);

        notificationService.createAndSend(
                request.getRequester(),
                "İsteğiniz kabul edildi",
                request.getVet().getFirstName() + " " + request.getVet().getLastName() + " müşteri isteğinizi kabul etti.",
                "VET_CUSTOMER_ACCEPTED",
                Map.of("vetId", String.valueOf(request.getVet().getUid()))
        );

        return toRequestResponse(saved);
    }

    @Transactional
    public VetCustomerRequestResponse reject(String vetEmail, Long requestId) {
        VetCustomerRequest request = findOwnedRequest(vetEmail, requestId);
        if (request.getStatus() != VetCustomerRequestStatus.PENDING) {
            throw new BusinessException("Bu istek zaten karara bağlanmış");
        }
        request.setStatus(VetCustomerRequestStatus.REJECTED);
        request.setDecidedAt(OffsetDateTime.now());
        VetCustomerRequest saved = vetCustomerRequestRepository.save(request);

        notificationService.createAndSend(
                request.getRequester(),
                "İsteğiniz reddedildi",
                request.getVet().getFirstName() + " " + request.getVet().getLastName() + " müşteri isteğinizi reddetti.",
                "VET_CUSTOMER_REJECTED",
                Map.of("vetId", String.valueOf(request.getVet().getUid()))
        );

        return toRequestResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VetCustomerResponse> listCustomers(String vetEmail, Pageable pageable) {
        User vet = findUserByEmail(vetEmail);
        return vetCustomerRequestRepository
                .findByVet_UidAndStatus(vet.getUid(), VetCustomerRequestStatus.ACCEPTED, pageable)
                .map(request -> new VetCustomerResponse(
                        request.getId(),
                        request.getRequester().getUid(),
                        request.getRequester().getFirstName() + " " + request.getRequester().getLastName(),
                        request.getStatus(),
                        request.getCreatedAt()
                ))
                .getContent();
    }

    /**
     * ÖNCE vet↔müşteri arasında ACCEPTED ilişki var mı doğrular -- bu kontrol
     * olmadan herhangi bir vet başka birinin müşterisinin hayvanlarını
     * görebilirdi (yetki açığı).
     */
    @Transactional(readOnly = true)
    public List<PetResponse> getCustomerPets(String vetEmail, Long customerId) {
        User vet = findUserByEmail(vetEmail);
        assertAcceptedRelationship(vet.getUid(), customerId);

        return petRepository.findByOwner_Uid(customerId).stream()
                .map(petService::toResponse)
                .toList();
    }

    /** {@code PetTreatmentNoteService} tarafından da kullanılan paylaşılan yetki kontrolü. */
    @Transactional(readOnly = true)
    public boolean hasAcceptedRelationship(Long vetUid, Long customerUid) {
        return vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(customerUid, vetUid)
                .map(r -> r.getStatus() == VetCustomerRequestStatus.ACCEPTED)
                .orElse(false);
    }

    private void assertAcceptedRelationship(Long vetUid, Long customerUid) {
        if (!hasAcceptedRelationship(vetUid, customerUid)) {
            throw new AccessDeniedException("Bu kullanıcı müşteriniz değil");
        }
    }

    private VetCustomerRequest findOwnedRequest(String vetEmail, Long requestId) {
        User vet = findUserByEmail(vetEmail);
        VetCustomerRequest request = vetCustomerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("İstek bulunamadı: " + requestId));
        if (!request.getVet().getUid().equals(vet.getUid())) {
            throw new AccessDeniedException("Bu istek size ait değil");
        }
        return request;
    }

    private VetCustomerRequestResponse toRequestResponse(VetCustomerRequest request) {
        return new VetCustomerRequestResponse(
                request.getId(),
                request.getRequester().getUid(),
                request.getRequester().getFirstName() + " " + request.getRequester().getLastName(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
