package vbotelho.dev.algorithm_sm2.service;


import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vbotelho.dev.algorithm_sm2.domain.entity.CardState;
import vbotelho.dev.algorithm_sm2.domain.entity.Deck;
import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;
import vbotelho.dev.algorithm_sm2.domain.repository.CardStateRepository;
import vbotelho.dev.algorithm_sm2.domain.repository.DeckRepository;
import vbotelho.dev.algorithm_sm2.domain.repository.ReviewLogRepository;
import vbotelho.dev.algorithm_sm2.service.dto.response.StatusResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.StudySessionResponse;
import vbotelho.dev.algorithm_sm2.service.exception.ApiException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final CardStateRepository cardStateRepository;
    private final DeckRepository deckRepository;
    private final ReviewLogRepository reviewLogRepository;

    private static final List<CardStatus> DUE_STATUSES =
            List.of(CardStatus.REVIEW, CardStatus.LEARNING, CardStatus.RELEARN);

    /**
     * Build a study session for a user: due cards + new cards up to deck limits.
     * Result is cached in Redis by userId for 5 minutes.
     */
    @Transactional(readOnly = true)
    @Timed(value = "sm2.session.build", description = "Time to build study session")
    @Cacheable(value = "studySession", key = "#userId")
    public StudySessionResponse buildSession(UUID userId, UUID deckId, int maxCards) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> ApiException.notFound("Deck", deckId));

        if (!deck.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("Deck does not belong to this user");
        }

        LocalDate today = LocalDate.now();
        int reviewLimit = Math.min(maxCards, deck.getReviewLimit());
        int newLimit = Math.min(maxCards, deck.getNewCardsLimit());

        // Due cards (REVIEW, LEARNING, RELEARN)
        List<CardState> dueCards = cardStateRepository.findDueCards(
                userId, DUE_STATUSES, today, PageRequest.of(0, reviewLimit));

        // New cards (fill remaining slots)
        int newSlots = Math.max(0, newLimit - dueCards.size());
        List<CardState> newCards = newSlots > 0
                ? cardStateRepository.findNewCards(userId, PageRequest.of(0, newSlots))
                : List.of();

        List<StudySessionResponse.StudyCardResponse> cards = Stream.concat(
                dueCards.stream(), newCards.stream())
                .map(this::toStudyCard)
                .collect(Collectors.toList());

        // Shuffle new cards in to avoid predictable ordering
        Collections.shuffle(cards);

        return new StudySessionResponse(cards, dueCards.size(), newCards.size());
    }

    /**
     * Stats for a user: counts by status, reviews today/this week.
     */
    @Transactional(readOnly = true)
    public StatusResponse getStats(UUID userId) {
        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant startOfWeek = today.minus(7, ChronoUnit.DAYS).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);

        List<Object[]> raw = cardStateRepository.countByStatusDueToday(userId, today);
        Map<String, Long> countByStatus = raw.stream()
                .collect(Collectors.toMap(r -> r[0].toString(), r -> (Long) r[1]));

        long totalDue = countByStatus.values().stream().mapToLong(Long::longValue).sum();
        long reviewsToday = reviewLogRepository.countReviewsSince(userId, startOfDay);
        long reviewsThisWeek = reviewLogRepository.countReviewsSince(userId, startOfWeek);

        return new StatusResponse(userId, countByStatus, totalDue,
                reviewsToday, reviewsThisWeek, today);
    }

    private StudySessionResponse.StudyCardResponse toStudyCard(CardState cs) {
        var card = cs.getCard();
        return new StudySessionResponse.StudyCardResponse(
                cs.getId(), card.getId(),
                card.getFront(), card.getBack(), card.getExtra(), card.getTags(),
                cs.getStatus(), cs.getRepetitions(), cs.getDueDate()
        );
    }
}
