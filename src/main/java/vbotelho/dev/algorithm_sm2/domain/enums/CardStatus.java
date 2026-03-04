package vbotelho.dev.algorithm_sm2.domain.enums;

public enum CardStatus {
    NEW,       // Never reviewed
    LEARNING,  // In initial learning phase
    REVIEW,    // Graduated — in spaced repetition
    RELEARN    // Failed review — back in learning
}
