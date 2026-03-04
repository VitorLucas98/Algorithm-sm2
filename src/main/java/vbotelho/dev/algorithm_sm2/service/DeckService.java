package vbotelho.dev.algorithm_sm2.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vbotelho.dev.algorithm_sm2.domain.entity.Card;
import vbotelho.dev.algorithm_sm2.domain.entity.Deck;
import vbotelho.dev.algorithm_sm2.domain.entity.User;
import vbotelho.dev.algorithm_sm2.domain.repository.CardRepository;
import vbotelho.dev.algorithm_sm2.domain.repository.CardStateRepository;
import vbotelho.dev.algorithm_sm2.domain.repository.DeckRepository;
import vbotelho.dev.algorithm_sm2.domain.repository.UserRepository;
import vbotelho.dev.algorithm_sm2.service.dto.request.CreateCardRequest;
import vbotelho.dev.algorithm_sm2.service.dto.request.CreateDeckRequest;
import vbotelho.dev.algorithm_sm2.service.dto.response.CardResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.DeckResponse;
import vbotelho.dev.algorithm_sm2.service.exception.ApiException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final CardStateRepository cardStateRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<DeckResponse> listDecks(UUID userId, Pageable pageable) {
        return deckRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional
    public DeckResponse createDeck(UUID userId, CreateDeckRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User", userId));
        Deck deck = Deck.builder()
                .user(user)
                .name(req.name())
                .description(req.description())
                .newCardsLimit(req.newCardsLimit() != null ? req.newCardsLimit() : 20)
                .reviewLimit(req.reviewLimit() != null ? req.reviewLimit() : 200)
                .build();
        return toResponse(deckRepository.save(deck));
    }

    @Transactional
    public CardResponse addCard(UUID userId, UUID deckId, CreateCardRequest req) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> ApiException.notFound("Deck", deckId));
        if (!deck.getUser().getId().equals(userId)) throw ApiException.forbidden("Not your deck");

        Card card = Card.builder()
                .deck(deck)
                .front(req.front())
                .back(req.back())
                .extra(req.extra())
                .tags(req.tags())
                .build();
        card = cardRepository.save(card);

        // Auto-initialize card state for the deck owner
        cardStateRepository.initializeStatesForDeck(deckId, userId);

        return toCardResponse(card);
    }

    @Transactional(readOnly = true)
    public Page<CardResponse> listCards(UUID userId, UUID deckId, Pageable pageable) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> ApiException.notFound("Deck", deckId));
        if (!deck.getUser().getId().equals(userId)) throw ApiException.forbidden("Not your deck");
        return cardRepository.findByDeckId(deckId, pageable).map(this::toCardResponse);
    }

    private DeckResponse toResponse(Deck d) {
        return new DeckResponse(d.getId(), d.getName(), d.getDescription(),
                d.getNewCardsLimit(), d.getReviewLimit(), d.getCreatedAt());
    }

    private CardResponse toCardResponse(Card c) {
        return new CardResponse(c.getId(), c.getDeck().getId(),
                c.getFront(), c.getBack(), c.getExtra(), c.getTags(), c.getCreatedAt());
    }
}
