package vbotelho.dev.algorithm_sm2.service.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DeckResponse(UUID id,
                           String name,
                           String description,
                           int newCardsLimit,
                           int reviewLimit,
                           Instant createdAt) {
}
