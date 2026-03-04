package vbotelho.dev.algorithm_sm2.service.dto.response;

import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StudySessionResponse(List<StudyCardResponse> cards,
                                   int totalDue,
                                   int totalNew) {
    public record StudyCardResponse(UUID cardStateId,
                             UUID cardId,
                             String front,
                             String back,
                             String extra,
                             List<String> tags,
                             CardStatus status,
                             int repetitions,
                             LocalDate dueDate) {
    }

}
