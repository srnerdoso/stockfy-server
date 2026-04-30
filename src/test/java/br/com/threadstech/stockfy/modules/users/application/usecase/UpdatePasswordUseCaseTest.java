package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.users.application.exception.CurrentPasswordInvalidException;
import br.com.threadstech.stockfy.modules.users.application.exception.InvalidPasswordResetCodeException;
import br.com.threadstech.stockfy.modules.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.modules.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

class UpdatePasswordUseCaseTest {

  private static final String RESET_CODE = "123456";
  private static final String RESET_CODE_HASH = "reset-code-hash";
  private static final String NEW_PASSWORD = "newPassword123";
  private static final String NEW_PASSWORD_HASH = "new-password-hash";
  private static final String CURRENT_PASSWORD = "currentPassword123";
  private static final String CURRENT_PASSWORD_HASH = "current-password-hash";
  private static final String INVALID_ATTEMPT_KEY_PREFIX = "password_update_attempts:";
  private static final int MAX_INVALID_ATTEMPTS = 15;

  private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
  private final ResetCodeHasher resetCodeHasher = org.mockito.Mockito.mock(ResetCodeHasher.class);
  private final StringRedisTemplate redisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
  private final ValueOperations<String, String> valueOperations =
      org.mockito.Mockito.mock(ValueOperations.class);
  private final UpdatePasswordUseCase useCase =
      new UpdatePasswordUseCase(userRepository, passwordEncoder, resetCodeHasher, redisTemplate);

  @Test
  @DisplayName("Deve atualizar senha e revogar codigo quando codigo for valido")
  void executeWithCode_whenCodeIsValid_thenUpdatesPasswordAndRevokesCode() {
    User user = user(UUID.randomUUID());
    user.setResetPasswordCodeHash(RESET_CODE_HASH);
    user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(10));
    when(resetCodeHasher.hash(RESET_CODE)).thenReturn(RESET_CODE_HASH);
    when(userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_HASH);

    useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD);

    assertEquals(NEW_PASSWORD_HASH, user.getPassword().value());
    assertNull(user.getResetPasswordCodeHash());
    assertNull(user.getResetPasswordExpiresAt());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve lancar InvalidPasswordResetCodeException quando codigo for invalido")
  void executeWithCode_whenCodeIsInvalid_thenThrowsInvalidPasswordResetCodeException() {
    when(resetCodeHasher.hash(RESET_CODE)).thenReturn(RESET_CODE_HASH);
    when(userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).thenReturn(Optional.empty());

    assertThrows(
        InvalidPasswordResetCodeException.class,
        () -> useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD));

    verify(userRepository, never()).update(any());
  }

  @Test
  @DisplayName("Deve lancar InvalidPasswordResetCodeException quando codigo estiver expirado")
  void executeWithCode_whenCodeIsExpired_thenThrowsInvalidPasswordResetCodeException() {
    User user = user(UUID.randomUUID());
    user.setResetPasswordCodeHash(RESET_CODE_HASH);
    user.setResetPasswordExpiresAt(LocalDateTime.now().minusMinutes(1));
    when(resetCodeHasher.hash(RESET_CODE)).thenReturn(RESET_CODE_HASH);
    when(userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).thenReturn(Optional.of(user));

    assertThrows(
        InvalidPasswordResetCodeException.class,
        () -> useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD));

    verify(userRepository, never()).update(any());
  }

  @Test
  @DisplayName("Deve buscar usuario pelo hash do codigo sem varrer usuarios")
  void executeWithCode_whenCalled_thenFindsUserByResetCodeHashWithoutScanningUsers() {
    User user = user(UUID.randomUUID());
    user.setResetPasswordCodeHash(RESET_CODE_HASH);
    user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(10));
    when(resetCodeHasher.hash(RESET_CODE)).thenReturn(RESET_CODE_HASH);
    when(userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_HASH);

    useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD);

    verify(userRepository).findByResetPasswordCodeHash(RESET_CODE_HASH);
    verify(userRepository, never()).findAll(any());
  }

  @Test
  @DisplayName("Deve atualizar senha quando senha atual conferir")
  void executeAuthenticated_whenCurrentPasswordMatches_thenUpdatesPassword() {
    UUID userId = UUID.randomUUID();
    User user = user(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_PASSWORD_HASH)).thenReturn(true);
    when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_HASH);

    useCase.executeAuthenticated(userId, CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);

    assertEquals(NEW_PASSWORD_HASH, user.getPassword().value());
    verify(redisTemplate).delete(INVALID_ATTEMPT_KEY_PREFIX + userId);
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve lancar CurrentPasswordInvalidException e contar tentativa quando senha atual falhar")
  void executeAuthenticated_whenCurrentPasswordIsWrong_thenThrowsCurrentPasswordInvalidExceptionAndCountsAttempt() {
    UUID userId = UUID.randomUUID();
    User user = user(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongPassword", CURRENT_PASSWORD_HASH)).thenReturn(false);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(INVALID_ATTEMPT_KEY_PREFIX + userId)).thenReturn(1L);

    assertThrows(
        CurrentPasswordInvalidException.class,
        () -> useCase.executeAuthenticated(userId, "wrongPassword", NEW_PASSWORD, NEW_PASSWORD));

    verify(valueOperations).increment(INVALID_ATTEMPT_KEY_PREFIX + userId);
    verify(redisTemplate).expire(INVALID_ATTEMPT_KEY_PREFIX + userId, Duration.ofMinutes(5));
    verify(userRepository, never()).update(user);
  }

  @Test
  @DisplayName("Deve lancar CurrentPasswordInvalidException quando senha atual for omitida")
  void executeAuthenticated_whenCurrentPasswordIsMissing_thenThrowsCurrentPasswordInvalidException() {
    UUID userId = UUID.randomUUID();
    User user = user(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(INVALID_ATTEMPT_KEY_PREFIX + userId)).thenReturn(1L);

    assertThrows(
        CurrentPasswordInvalidException.class,
        () -> useCase.executeAuthenticated(userId, null, NEW_PASSWORD, NEW_PASSWORD));

    verify(passwordEncoder, never()).matches(any(), any());
    verify(valueOperations).increment(INVALID_ATTEMPT_KEY_PREFIX + userId);
    verify(userRepository, never()).update(user);
  }

  @Test
  @DisplayName("Deve bloquear usuario na decima quinta tentativa invalida")
  void executeAuthenticated_whenFifteenthInvalidAttempt_thenLocksUser() {
    UUID userId = UUID.randomUUID();
    User user = user(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongPassword", CURRENT_PASSWORD_HASH)).thenReturn(false);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(INVALID_ATTEMPT_KEY_PREFIX + userId))
        .thenReturn((long) MAX_INVALID_ATTEMPTS);

    assertThrows(
        CurrentPasswordInvalidException.class,
        () -> useCase.executeAuthenticated(userId, "wrongPassword", NEW_PASSWORD, NEW_PASSWORD));

    assertEquals(UserStatus.LOCKED, user.getStatus());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve lancar PasswordMismatchException quando confirmacao nao conferir")
  void execute_whenPasswordsDoNotMatch_thenThrowsPasswordMismatchException() {
    UUID userId = UUID.randomUUID();

    assertThrows(
        PasswordMismatchException.class,
        () ->
            useCase.executeAuthenticated(
                userId, CURRENT_PASSWORD, NEW_PASSWORD, "differentPassword"));

    verifyNoInteractions(userRepository, passwordEncoder, resetCodeHasher, redisTemplate);
  }

  private User user(UUID id) {
    return User.builder()
        .id(id)
        .name("John Doe")
        .email(new Email("john@example.com"))
        .password(new Password(CURRENT_PASSWORD_HASH))
        .roles(Set.of(UserRole.USER))
        .status(UserStatus.ACTIVE)
        .active(true)
        .build();
  }
}
