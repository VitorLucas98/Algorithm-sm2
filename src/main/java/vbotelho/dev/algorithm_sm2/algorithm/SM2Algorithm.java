package vbotelho.dev.algorithm_sm2.algorithm;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vbotelho.dev.algorithm_sm2.domain.entity.CardState;
import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.LocalDate;

/**
 * Pure implementation of the SM-2 (SuperMemo 2) spaced repetition algorithm.
 *
 * <p>Algorithm rules:
 * <ul>
 *   <li>Quality 0-1: Complete failure — reset to beginning (interval=1, rep=0)</li>
 *   <li>Quality 2: Incorrect but remembered when shown — reset to beginning</li>
 *   <li>Quality 3: Correct with serious difficulty</li>
 *   <li>Quality 4: Correct with some hesitation</li>
 *   <li>Quality 5: Perfect response</li>
 * </ul>
 *
 * <p>EF formula: EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
 * <p>Interval rules:
 * <ul>
 *   <li>n=0 (first review): I=1</li>
 *   <li>n=1: I=6</li>
 *   <li>n>1: I = round(I_prev * EF)</li>
 * </ul>
 *
 * @see <a href="https://www.supermemo.com/en/archives1990-2015/english/ol/sm2">SM-2 Algorithm</a>
 */
@Component
public class SM2Algorithm {

    private final double minEaseFactor;
    private final int graduatingInterval;
    private final int easyInterval;

    public SM2Algorithm(
            @Value("${app.sm2.min-ease-factor:1.3}") double minEaseFactor,
            @Value("${app.sm2.graduating-interval:1}") int graduatingInterval,
            @Value("${app.sm2.easy-interval:4}") int easyInterval) {
        this.minEaseFactor = minEaseFactor;
        this.graduatingInterval = graduatingInterval;
        this.easyInterval = easyInterval;
    }

    /**
     * Compute the next review schedule for a card given the user's quality rating.
     *
     * @param state   current immutable state snapshot
     * @param quality user rating 0-5
     * @return computed result with next interval, EF, repetitions, status and due date
     */
    public SM2Result compute(CardStateSnapshot state, int quality) {
        validateQuality(quality);

        if (quality < 3) {
            // Failed review: reset repetitions, keep EF, schedule short review
            return SM2Result.builder()
                    .repetitions(0)
                    .intervalDays(1)
                    .easeFactor(Math.max(minEaseFactor, state.easeFactor() - 0.20))
                    .status(CardStatus.RELEARN)
                    .dueDate(LocalDate.now().plusDays(1))
                    .build();
        }

        // Successful review
        double newEF = computeEaseFactor(state.easeFactor(), quality);
        int newRepetitions = state.repetitions() + 1;
        int newInterval = computeInterval(state.intervalDays(), state.repetitions(), newEF, quality);
        CardStatus newStatus = determineStatus(state.status(), quality);

        return SM2Result.builder()
                .repetitions(newRepetitions)
                .intervalDays(newInterval)
                .easeFactor(newEF)
                .status(newStatus)
                .dueDate(LocalDate.now().plusDays(newInterval))
                .build();
    }

    public CardStateSnapshot snapshot(CardState state) {
        return new CardStateSnapshot(
                state.getEaseFactor().doubleValue(),
                state.getIntervalDays(),
                state.getRepetitions(),
                state.getStatus()
        );
    }


    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private double computeEaseFactor(double ef, int quality) {
        // EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
        double delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
        return Math.max(minEaseFactor, ef + delta);
    }

    private int computeInterval(int prevInterval, int repetitions, double ef, int quality) {
        // Easy bonus for quality=5
        if (repetitions == 0) {
            return (quality == 5) ? easyInterval : graduatingInterval;
        }
        if (repetitions == 1) {
            return 6;
        }
        return (int) Math.round(prevInterval * ef);
    }

    private CardStatus determineStatus(CardStatus current, int quality) {
        if (current == CardStatus.NEW || current == CardStatus.LEARNING) {
            return CardStatus.REVIEW;
        }
        if (current == CardStatus.RELEARN) {
            return CardStatus.REVIEW;
        }
        return CardStatus.REVIEW;
    }

    private void validateQuality(int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5, got: " + quality);
        }
    }

}

