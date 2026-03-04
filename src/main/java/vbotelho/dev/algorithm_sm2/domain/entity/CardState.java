package vbotelho.dev.algorithm_sm2.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import vbotelho.dev.algorithm_sm2.algorithm.SM2Result;
import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "card_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardState {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SM-2 core fields
    @Column(name = "ease_factor", nullable = false, precision = 4, scale = 2)
    private BigDecimal easeFactor = BigDecimal.valueOf(2.50);

    @Column(name = "interval_days", nullable = false)
    private int intervalDays = 0;

    @Column(name = "repetitions", nullable = false)
    private int repetitions = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.NEW;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "last_reviewed")
    private Instant lastReviewed;

    @Column(name = "total_reviews", nullable = false)
    private int totalReviews = 0;

    @Column(name = "lapse_count", nullable = false)
    private int lapseCount = 0;

    @Version
    @Column(nullable = false)
    private int version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dueDate == null) dueDate = LocalDate.now();
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Apply the SM-2 result to this entity.
     * Encapsulates mutation logic within the aggregate.
     */
    public void applyResult(SM2Result result) {
        boolean failed = result.getRepetitions() == 0 && this.repetitions > 0;
        if (failed) lapseCount++;

        this.easeFactor = BigDecimal.valueOf(result.getEaseFactor());
        this.intervalDays = result.getIntervalDays();
        this.repetitions = result.getRepetitions();
        this.status = result.getStatus();
        this.dueDate = result.getDueDate();
        this.lastReviewed = Instant.now();
        this.totalReviews++;
    }
}