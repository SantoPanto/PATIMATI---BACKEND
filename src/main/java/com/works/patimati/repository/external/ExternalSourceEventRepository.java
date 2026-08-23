package com.works.patimati.repository.external;

import com.works.patimati.entity.enums.TriggerType;
import com.works.patimati.entity.external.ExternalSourceEvent;
import com.works.patimati.entity.external.ExternalSourcePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalSourceEventRepository extends JpaRepository<ExternalSourceEvent, Long> {

    // POST_TAG/CAPTION_MENTION tekilliği — DB'deki kısmi indeksin ayna görüntüsü,
    // insert öncesi kontrol edilerek gereksiz constraint-violation exception'ı önlenir.
    boolean existsByPostAndTriggerType(ExternalSourcePost post, TriggerType triggerType);

    // COMMENT_MENTION tekilliği — aynı Instagram yorumu iki event üretmesin.
    boolean existsByPostAndExternalTriggerId(ExternalSourcePost post, String externalTriggerId);

    // Metin analizi tetikleyicisi: caption boş/alakasız olsa bile yorumda
    // anlamlı metin olabilir (Faz 2 revize blueprint §4 düzeltmesi).
    Optional<ExternalSourceEvent> findFirstByPostAndTriggeringCommentIsNotNullOrderByDetectedAtDesc(
            ExternalSourcePost post);
}
