package vbotelho.dev.algorithm_sm2.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vbotelho.dev.algorithm_sm2.service.ReviewService;
import vbotelho.dev.algorithm_sm2.service.StudySessionService;
import vbotelho.dev.algorithm_sm2.service.dto.request.BulkReviewRequest;
import vbotelho.dev.algorithm_sm2.service.dto.request.ReviewCardRequest;
import vbotelho.dev.algorithm_sm2.service.dto.response.BulkReviewResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.ReviewResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.StatusResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.StudySessionResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/study")
@RequiredArgsConstructor
@Tag(name = "Study")
public class StudyController {

    private final StudySessionService studySessionService;
    private final ReviewService reviewService;

    @GetMapping("/session")
    @Operation(summary = "Get next study session (due + new cards)")
    public StudySessionResponse getSession(
            @PathVariable UUID userId,
            @RequestParam UUID deckId,
            @RequestParam(defaultValue = "50") int maxCards) {
        return studySessionService.buildSession(userId, deckId, maxCards);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get study statistics for a user")
    public StatusResponse getStats(@PathVariable UUID userId) {
        return studySessionService.getStats(userId);
    }

    @PostMapping("/review/{cardStateId}")
    @Operation(summary = "Submit a single card review (quality 0-5)")
    public ReviewResponse review(
            @PathVariable UUID userId,
            @PathVariable UUID cardStateId,
            @Valid @RequestBody ReviewCardRequest req) {
        return reviewService.review(cardStateId, userId, req);
    }

    @PostMapping("/review/bulk")
    @Operation(summary = "Submit multiple card reviews in one request (for mobile sync)")
    public BulkReviewResponse bulkReview(
            @PathVariable UUID userId,
            @Valid @RequestBody BulkReviewRequest req) {
        return reviewService.bulkReview(userId, req);
    }
}
