package vbotelho.dev.algorithm_sm2.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vbotelho.dev.algorithm_sm2.domain.entity.User;
import vbotelho.dev.algorithm_sm2.domain.repository.UserRepository;
import vbotelho.dev.algorithm_sm2.service.dto.request.CreateUserRequest;
import vbotelho.dev.algorithm_sm2.service.dto.response.UserResponse;
import vbotelho.dev.algorithm_sm2.service.exception.ApiException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmail(req.email()))
            throw ApiException.conflict("Email already in use: " + req.email());
        if (userRepository.existsByUsername(req.username()))
            throw ApiException.conflict("Username already in use: " + req.username());

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .build();
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> ApiException.notFound("User", userId));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getCreatedAt());
    }
}
