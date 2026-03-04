package vbotelho.dev.algorithm_sm2.service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewCardRequest(@NotNull @Min(0) @Max(5) Integer quality,
                                Integer timeTakenMs) {
}
