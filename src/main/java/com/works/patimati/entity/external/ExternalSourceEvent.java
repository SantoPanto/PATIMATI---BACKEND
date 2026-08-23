package com.works.patimati.entity.external;

import com.works.patimati.entity.enums.TriggerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Bir gönderiyi bize işaret eden tek bir tetikleyici (tag/mention/comment).
 *
 * <p>Benzersizlik veritabanı seviyesinde iki kısmi indeksle sağlanır (bkz.
 * ilgili migration): POST_TAG/CAPTION_MENTION gönderi başına en fazla bir
 * kez, COMMENT_MENTION ise yorum kimliği başına en fazla bir kez.
 */
@Entity
@Table(name = "external_source_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalSourceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_source_post_id", nullable = false)
    private ExternalSourcePost post;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 16)
    private TriggerType triggerType;

    /** Instagram yorum kimliği; POST_TAG/CAPTION_MENTION için null. */
    @Column(name = "external_trigger_id", length = 64)
    private String externalTriggerId;

    @Column(name = "triggering_comment", columnDefinition = "text")
    private String triggeringComment;

    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;
}
