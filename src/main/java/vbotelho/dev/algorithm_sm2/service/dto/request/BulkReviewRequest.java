package vbotelho.dev.algorithm_sm2.service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkReviewRequest(@NotEmpty @Size(max = 100) List<ReviewItem> reviews) {
    public record ReviewItem(
            @NotNull UUID cardStateId,
            @NotNull @Min(0) @Max(5) Integer quality,
            Integer timeTakenMs
    ) {}
}
