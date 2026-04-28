package br.com.threadstech.stockfy.modules.users.presentation.controller;

import br.com.threadstech.stockfy.modules.users.application.dto.FindAllUsersResponse;
import br.com.threadstech.stockfy.modules.users.application.dto.RegisterUserRequest;
import br.com.threadstech.stockfy.modules.users.application.dto.UserListItemResponse;
import br.com.threadstech.stockfy.modules.users.application.dto.UserListType;
import br.com.threadstech.stockfy.modules.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.modules.users.application.usecase.DeleteUserUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.FindAllUsersUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.GenerateResetCodeUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.RegisterUserUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.ResetPasswordUseCase;
import br.com.threadstech.stockfy.modules.users.application.usecase.UpdateProfileUseCase;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.presentation.mapper.UserResponseMapperFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final RegisterUserUseCase registerUserUseCase;
  private final FindAllUsersUseCase findAllUsersUseCase;
  private final UpdateProfileUseCase updateProfileUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final GenerateResetCodeUseCase generateResetCodeUseCase;
  private final ResetPasswordUseCase resetPasswordUseCase;
  private final UserRepository userRepository;
  private final UserResponseMapperFactory mapperFactory;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest request) {
    registerUserUseCase.execute(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{id}/password-reset-codes")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResetCodeResponse> generateResetCode(@PathVariable UUID id) {
    String code = generateResetCodeUseCase.execute(id);
    return ResponseEntity.status(HttpStatus.CREATED).body(new ResetCodeResponse(code));
  }

  @PatchMapping("/password")
  public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
    resetPasswordUseCase.execute(request.email(), request.code(), request.newPassword());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(Authentication authentication) {
    UUID userId = (UUID) authentication.getPrincipal();
    return userRepository
        .findById(userId)
        .map(mapperFactory::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
    return userRepository
        .findById(id)
        .map(mapperFactory::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<FindAllUsersResponse<UserListItemResponse>> findAll(
      @RequestParam(required = false)
          @Size(max = 255, message = "{user.name.size}")
          @Pattern(regexp = "^[\\p{L}\\p{M}0-9 .'-]+$", message = "{user.name.pattern}")
          String name,
      @RequestParam UserListType type,
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(findAllUsersUseCase.execute(name, type, pageable));
  }

  @PatchMapping("/me")
  public ResponseEntity<Void> updateProfile(
      @RequestBody UpdateProfileRequest request, Authentication authentication) {
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

  public record UpdateProfileRequest(String name, String email) {}

  public record ResetCodeResponse(String code) {}

  public record ResetPasswordRequest(String email, String code, String newPassword) {}
}
