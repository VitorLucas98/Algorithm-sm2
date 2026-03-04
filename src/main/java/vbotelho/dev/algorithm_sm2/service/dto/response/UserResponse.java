package vbotelho.dev.algorithm_sm2.service.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse (UUID id,
                            String username,
                            String email,
                            Instant createdAt) {
}
