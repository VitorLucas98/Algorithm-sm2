package vbotelho.dev.algorithm_sm2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vbotelho.dev.algorithm_sm2.service.DeckService;
import vbotelho.dev.algorithm_sm2.service.dto.request.CreateCardRequest;
import vbotelho.dev.algorithm_sm2.service.dto.request.CreateDeckRequest;
import vbotelho.dev.algorithm_sm2.service.dto.response.CardResponse;
import vbotelho.dev.algorithm_sm2.service.dto.response.DeckResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/decks")
@RequiredArgsConstructor
@Tag(name = "Decks")
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    @Operation(summary = "List user decks")
    public Page<DeckResponse> list(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return deckService.listDecks(userId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new deck")
    public DeckResponse create(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateDeckRequest req) {
        return deckService.createDeck(userId, req);
    }

    @GetMapping("/{deckId}/cards")
    @Operation(summary = "List cards in a deck")
    public Page<CardResponse> listCards(
            @PathVariable UUID userId,
            @PathVariable UUID deckId,
            @PageableDefault(size = 50) Pageable pageable) {
        return deckService.listCards(userId, deckId, pageable);
    }

    @PostMapping("/{deckId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add card to a deck")
    public CardResponse addCard(
            @PathVariable UUID userId,
            @PathVariable UUID deckId,
            @Valid @RequestBody CreateCardRequest req) {
        return deckService.addCard(userId, deckId, req);
    }
}
