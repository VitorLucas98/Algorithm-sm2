package vbotelho.dev.algorithm_sm2.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vbotelho.dev.algorithm_sm2.algorithm.SM2Algorithm;
import vbotelho.dev.algorithm_sm2.domain.entity.CardState;
import vbotelho.dev.algorithm_sm2.domain.entity.ReviewLog;
import vbotelho.dev.algorithm_sm2.domain.repository.CardStateRepository;
import vbotelho.dev.algorithm_sm2.domain.repository.ReviewLogRepository;
import vbotelho.dev.algorithm_sm2.service.dto.request.BulkReviewRequest;
import vbotelho.dev.algorithm_sm2.service.dto.request.ReviewCardRequest;
import vbotelho.dev.algorithm_sm2.service.dto.response.BulkReviewResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.ReviewResponse;
import vbotelho.dev.algorithm_sm2.service.exception.ApiException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final CardStateRepository cardStateRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final SM2Algorithm sm2Algorithm;
    private final MeterRegistry meterRegistry;

    /**
     * Process a single card review.
     * Uses @Version optimistic locking to prevent concurrent state corruption.
     */
    @Transactional
    @Timed(value = "sm2.review.single", description = "Time to process a single review")
    @CacheEvict(value = "studySession", key = "#userId")
    public ReviewResponse review(UUID cardStateId, UUID userId, ReviewCardRequest req) {
        CardState state = cardStateRepository.findById(cardStateId)
                .orElseThrow(() -> ApiException.notFound("CardState", cardStateId));

        if (!state.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("Card state does not belong to this user");
        }

        // Snapshot before mutation (for audit log)
        var snapshot = sm2Algorithm.snapshot(state);
        var statusBefore = state.getStatus();
        var intervalBefore = state.getIntervalDays();
        var efBefore = state.getEaseFactor();
        var repsBefore = state.getRepetitions();

        // Run SM-2
        var result = sm2Algorithm.compute(snapshot, req.quality());

        // Apply result to aggregate
        state.applyResult(result);
        cardStateRepository.save(state);

        // Build immutable audit log
        var log = ReviewLog.builder()
                .cardStateId(cardStateId)
                .userId(userId)
                .cardId(state.getCard().getId())
                .quality(req.quality())
                .easeFactorBefore(efBefore.doubleValue())
                .easeFactorAfter(result.getEaseFactor())
                .intervalBefore(intervalBefore)
                .intervalAfter(result.getIntervalDays())
                .repetitionsBefore(repsBefore)
                .repetitionsAfter(result.getRepetitions())
                .statusBefore(statusBefore)
                .statusAfter(result.getStatus())
                .timeTakenMs(req.timeTakenMs())
                .reviewedAt(Instant.now())
                .build();
        reviewLogRepository.save(log);

        // Metrics
        Counter.builder("sm2.reviews.total")
                .tag("quality", String.valueOf(req.quality()))
                .tag("status_from", statusBefore.name())
                .tag("status_to", result.getStatus().name())
                .register(meterRegistry)
                .increment();

        return new ReviewResponse(
                log.getId(), cardStateId,
                req.quality(),
                efBefore.doubleValue(), result.getEaseFactor(),
                intervalBefore, result.getIntervalDays(),
                statusBefore, result.getStatus(),
                result.getDueDate(),
                log.getReviewedAt()
        );
    }

    /**
     * Bulk review — processes multiple cards in a single transaction.
     * Designed for offline sync scenarios (mobile apps, batch submission).
     */
    @Transactional
    @Timed(value = "sm2.review.bulk", description = "Time to process bulk reviews")
    @CacheEvict(value = "studySession", key = "#userId")
    public BulkReviewResponse bulkReview(UUID userId, BulkReviewRequest req) {
        List<ReviewResponse> results = new ArrayList<>();
        int failed = 0;

        for (var item : req.reviews()) {
            try {
                var singleReq = new ReviewCardRequest(item.quality(), item.timeTakenMs());
                results.add(review(item.cardStateId(), userId, singleReq));
            } catch (Exception e) {
                log.warn("Failed to process review for cardState {}: {}", item.cardStateId(), e.getMessage());
                failed++;
            }
        }

        return new BulkReviewResponse(results.size(), failed, results);
    }
}
