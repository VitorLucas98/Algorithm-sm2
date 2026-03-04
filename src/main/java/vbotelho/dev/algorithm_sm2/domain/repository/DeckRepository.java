package vbotelho.dev.algorithm_sm2.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vbotelho.dev.algorithm_sm2.domain.entity.Deck;

import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, UUID> {
    @Query("SELECT d FROM Deck d WHERE d.user.id = :userId AND d.archived = false")
    Page<Deck> findByUserId(@Param("userId") UUID userId, Pageable pageable);
}
