package vbotelho.dev.algorithm_sm2.service.dto.response;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record StatusResponse(UUID userId,
                             Map<String, Long> countByStatus,
                             long totalDueToday,
                             long reviewsToday,
                             long reviewsThisWeek,
                             LocalDate date) {
}
