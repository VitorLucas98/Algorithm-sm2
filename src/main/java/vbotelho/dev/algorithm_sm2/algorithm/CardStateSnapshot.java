package vbotelho.dev.algorithm_sm2.algorithm;


// -----------------------------------------------------------------------
// Value objects (records — zero allocation overhead vs full entity copy)
// -----------------------------------------------------------------------

import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

public record CardStateSnapshot(
        double easeFactor,
        int intervalDays,
        int repetitions,
        CardStatus status
) {}
