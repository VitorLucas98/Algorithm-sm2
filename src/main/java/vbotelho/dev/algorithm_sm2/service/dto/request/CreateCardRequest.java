package vbotelho.dev.algorithm_sm2.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCardRequest(@NotBlank String front,
                                @NotBlank String back,
                                String extra,
                                List<@Size(max = 50) String> tags) {
}
