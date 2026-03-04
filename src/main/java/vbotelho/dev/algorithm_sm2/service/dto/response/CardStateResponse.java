package vbotelho.dev.algorithm_sm2.service.dto.response;

import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CardStateResponse(UUID id,
                                UUID cardId,
                                double easeFactor,
                                int intervalDays,
                                int repetitions,
                                CardStatus status,
                                LocalDate dueDate,
                                Instant lastReviewed,
                                int totalReviews,
                                int lapseCount) {
}
