package com.works.patimati.repository.external;

import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalSourceMediaRepository extends JpaRepository<ExternalSourceMedia, Long> {

    Optional<ExternalSourceMedia> findByPostAndOrdinal(ExternalSourcePost post, short ordinal);

    List<ExternalSourceMedia> findByPostOrderByOrdinalAsc(ExternalSourcePost post);

    /** {@link #findByPostOrderByOrdinalAsc} ile aynı, sayfa içindeki tüm gönderiler için TEK sorguda (N+1'i önler). */
    List<ExternalSourceMedia> findByPostInOrderByOrdinalAsc(List<ExternalSourcePost> posts);

    long countByPostAndProcessingState(ExternalSourcePost post, ExternalSourceMedia.ProcessingState state);

    Optional<ExternalSourceMedia> findByPostAndContentSha256AndProcessingState(
            ExternalSourcePost post, String contentSha256, ExternalSourceMedia.ProcessingState state);
}
