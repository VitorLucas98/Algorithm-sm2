package vbotelho.dev.algorithm_sm2.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vbotelho.dev.algorithm_sm2.domain.entity.ReviewLog;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface ReviewLogRepository extends JpaRepository<ReviewLog, UUID> {

    @Query("SELECT rl FROM ReviewLog rl WHERE rl.userId = :userId ORDER BY rl.reviewedAt DESC")
    Page<ReviewLog> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT rl FROM ReviewLog rl
            WHERE rl.userId = :userId
              AND rl.reviewedAt BETWEEN :from AND :to
            ORDER BY rl.reviewedAt DESC
            """)
    Page<ReviewLog> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("SELECT COUNT(rl) FROM ReviewLog rl WHERE rl.userId = :userId AND rl.reviewedAt >= :since")
    long countReviewsSince(@Param("userId") UUID userId, @Param("since") Instant since);
}
