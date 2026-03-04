package vbotelho.dev.algorithm_sm2.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewLog {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "card_state_id", nullable = false)
    private UUID cardStateId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(nullable = false)
    private int quality;

    @Column(name = "ease_factor_before", nullable = false)
    private double easeFactorBefore;

    @Column(name = "ease_factor_after", nullable = false)
    private double easeFactorAfter;

    @Column(name = "interval_before", nullable = false)
    private int intervalBefore;

    @Column(name = "interval_after", nullable = false)
    private int intervalAfter;

    @Column(name = "repetitions_before", nullable = false)
    private int repetitionsBefore;

    @Column(name = "repetitions_after", nullable = false)
    private int repetitionsAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before", nullable = false, length = 20)
    private CardStatus statusBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after", nullable = false, length = 20)
    private CardStatus statusAfter;

    @Column(name = "time_taken_ms")
    private Integer timeTakenMs;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;

    @PrePersist
    protected void onCreate() {
        if (reviewedAt == null) reviewedAt = Instant.now();
    }
}

