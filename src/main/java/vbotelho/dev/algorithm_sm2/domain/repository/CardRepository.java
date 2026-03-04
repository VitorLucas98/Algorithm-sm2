package vbotelho.dev.algorithm_sm2.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vbotelho.dev.algorithm_sm2.domain.entity.Card;
import org.springframework.data.repository.query.Param;


import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT c FROM Card c WHERE c.deck.id = :deckId AND c.archived = false")
    Page<Card> findByDeckId(@Param("deckId") UUID deckId, Pageable pageable);
}
