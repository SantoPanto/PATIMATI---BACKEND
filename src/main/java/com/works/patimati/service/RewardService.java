package com.works.patimati.service;

import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

    private final UserRepository userRepository;

    /**
     * Finder earns 1 lost point if finderId is provided.
     */
    @Transactional
    public void awardLostPoint(Long finderId) {
        if (finderId == null) {
            log.info("Lost ad resolved without specifying a finder.");
            return;
        }

        User finder = userRepository.findById(finderId)
                .orElseThrow(() -> new ResourceNotFoundException("Bulunan kullanıcı (finder) bulunamadı ID: " + finderId));

        finder.setLostPoints(finder.getLostPoints() + 1);
        userRepository.save(finder);
        log.info("Awarded 1 lost point to finder. finderId={}, newTotal={}", finderId, finder.getLostPoints());
    }

    /**
     * Owner earns 1 adoption point, and adopter (if provided) earns 1 adoption point.
     */
    @Transactional
    public void awardAdoptionPoints(Long ownerId, Long adopterId) {
        if (ownerId != null) {
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("İlan sahibi kullanıcı bulunamadı ID: " + ownerId));

            owner.setAdoptionPoints(owner.getAdoptionPoints() + 1);
            userRepository.save(owner);
            log.info("Awarded 1 adoption point to owner. ownerId={}, newTotal={}", ownerId, owner.getAdoptionPoints());
        }

        if (adopterId != null) {
            if (adopterId.equals(ownerId)) {
                log.info("Adopter and owner are the same user ID (ownerId={}). Point already awarded once.", ownerId);
                return;
            }

            User adopter = userRepository.findById(adopterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sahiplenen kullanıcı (adopter) bulunamadı ID: " + adopterId));

            adopter.setAdoptionPoints(adopter.getAdoptionPoints() + 1);
            userRepository.save(adopter);
            log.info("Awarded 1 adoption point to adopter. adopterId={}, newTotal={}", adopterId, adopter.getAdoptionPoints());
        }
    }
}
