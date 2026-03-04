package vbotelho.dev.algorithm_sm2.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vbotelho.dev.algorithm_sm2.service.UserService;
import vbotelho.dev.algorithm_sm2.service.dto.request.CreateUserRequest;
import vbotelho.dev.algorithm_sm2.service.dto.response.UserResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user")
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.createUser(req);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public UserResponse get(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }
}
