package vbotelho.dev.algorithm_sm2.service.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CardResponse(UUID id,
                           UUID deckId,
                           String front,
                           String back,
                           String extra,
                           List<String> tags,
                           Instant createdAt) {
}
