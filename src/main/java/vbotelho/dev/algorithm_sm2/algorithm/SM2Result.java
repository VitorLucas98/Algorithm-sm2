package vbotelho.dev.algorithm_sm2.algorithm;

import lombok.Builder;
import lombok.Value;
import vbotelho.dev.algorithm_sm2.domain.enums.CardStatus;

import java.time.LocalDate;

@Value
@Builder
public class SM2Result {
    int repetitions;
    int intervalDays;
    double easeFactor;
    CardStatus status;
    LocalDate dueDate;
}
