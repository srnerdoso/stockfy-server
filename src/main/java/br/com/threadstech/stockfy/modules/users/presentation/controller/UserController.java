package br.com.threadstech.stockfy.modules.users.presentation.controller;

import br.com.threadstech.stockfy.modules.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.modules.users.application.usecase.*;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.presentation.mapper.UserResponseMapperFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UserRepository userRepository;
    private final UserResponseMapperFactory mapperFactory;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        registerUserUseCase.execute(request.name(), request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return userRepository.findById(userId)
                .map(mapperFactory::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(mapperFactory::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> findAll(@RequestParam(required = false) String name) {
        List<UserResponse> users = userRepository.findAll(name).stream()
                .map(mapperFactory::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        updateProfileUseCase.execute(userId, request.name(), request.email());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    public record RegisterRequest(String name, String email, String password, UserRole role) {}
    public record UpdateProfileRequest(String name, String email) {}
}
