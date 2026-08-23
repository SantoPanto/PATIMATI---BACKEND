package com.works.patimati.repository.external;

import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.enums.ExternalSource;
import com.works.patimati.entity.external.ExternalSourcePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalSourcePostRepository extends JpaRepository<ExternalSourcePost, Long> {

    // Kanonik kimlik — dedup'ın dayandığı sorgu (blueprint §18).
    Optional<ExternalSourcePost> findBySourceAndSourcePostId(ExternalSource source, String sourcePostId);

    // ExternalIngestionMaintenanceJob'un süpürdüğü, takılı kalmış gönderiler.
    @Query("SELECT p FROM ExternalSourcePost p WHERE p.processingStatus = :status AND p.updatedAt < :cutoff")
    List<ExternalSourcePost> findStuckAt(
            @Param("status") ExternalProcessingStatus status,
            @Param("cutoff") Instant cutoff);
}
