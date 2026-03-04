package vbotelho.dev.algorithm_sm2.service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeckRequest(@NotBlank @Size(max = 255) String name,
                                String description,
                                @Min(1) @Max(500) Integer newCardsLimit,
                                @Min(1) @Max(9999) Integer reviewLimit) {
}
