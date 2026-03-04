package vbotelho.dev.algorithm_sm2.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vbotelho.dev.algorithm_sm2.domain.entity.CardState;
import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardStateRepository extends JpaRepository<CardState, UUID> {

    /**
     * Fetch due cards for a user (cards where dueDate <= today and not NEW).
     * Uses the composite index (user_id, due_date, status).
     */
    @Query("""
            SELECT cs FROM CardState cs
            JOIN FETCH cs.card c
            WHERE cs.user.id = :userId
              AND cs.status IN :statuses
              AND cs.dueDate <= :today
            ORDER BY cs.dueDate ASC
            """)
    List<CardState> findDueCards(
            @Param("userId") UUID userId,
            @Param("statuses") List<CardStatus> statuses,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    /**
     * Fetch new cards for a user (never reviewed).
     */
    @Query("""
            SELECT cs FROM CardState cs
            JOIN FETCH cs.card c
            WHERE cs.user.id = :userId
              AND cs.status = 'NEW'
            ORDER BY cs.createdAt ASC
            """)
    List<CardState> findNewCards(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("SELECT cs FROM CardState cs WHERE cs.card.id = :cardId AND cs.user.id = :userId")
    Optional<CardState> findByCardIdAndUserId(
            @Param("cardId") UUID cardId,
            @Param("userId") UUID userId
    );

    /**
     * Count cards due today per status — used for dashboard stats.
     */
    @Query("""
            SELECT cs.status, COUNT(cs)
            FROM CardState cs
            WHERE cs.user.id = :userId
              AND cs.dueDate <= :today
            GROUP BY cs.status
            """)
    List<Object[]> countByStatusDueToday(
            @Param("userId") UUID userId,
            @Param("today") LocalDate today
    );

    /**
     * Bulk-initialize card states when a deck is assigned to a new user.
     */
    @Modifying
    @Query(value = """
            INSERT INTO card_states (id, card_id, user_id, ease_factor, interval_days,
                                     repetitions, status, due_date, created_at, updated_at, version)
            SELECT gen_random_uuid(), c.id, :userId, 2.50, 0, 0, 'NEW', CURRENT_DATE, now(), now(), 0
            FROM cards c
            WHERE c.deck_id = :deckId
              AND c.archived = false
              AND NOT EXISTS (
                  SELECT 1 FROM card_states cs
                  WHERE cs.card_id = c.id AND cs.user_id = :userId
              )
            """, nativeQuery = true)
    int initializeStatesForDeck(@Param("deckId") UUID deckId, @Param("userId") UUID userId);
}

