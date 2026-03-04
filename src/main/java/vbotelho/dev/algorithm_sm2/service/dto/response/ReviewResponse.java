package vbotelho.dev.algorithm_sm2.service.dto.response;

import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReviewResponse(UUID reviewLogId,
                             UUID cardStateId,
                             int quality,
                             double easeFactorBefore,
                             double easeFactorAfter,
                             int intervalBefore,
                             int intervalAfter,
                             CardStatus statusBefore,
                             CardStatus statusAfter,
                             LocalDate nextDueDate,
                             Instant reviewedAt) {
}
