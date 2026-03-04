package vbotelho.dev.algorithm_sm2.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank @Size(min = 3, max = 50) String username,
                                @NotBlank @Email String email) {
}
