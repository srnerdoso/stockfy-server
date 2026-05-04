package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.usecase.UnlockUserUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class UnlockUserUseCaseTest {

  private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";

  private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
  private final StringRedisTemplate redisTemplate =
      org.mockito.Mockito.mock(StringRedisTemplate.class);
  private final UnlockUserUseCase useCase = new UnlockUserUseCase(userRepository, redisTemplate);

  @Test
  @DisplayName("Deve desbloquear usuario bloqueado e limpar tentativas de login")
  void execute_whenUserIsLocked_thenUnlocksUserAndClearsLoginAttempts() {
    UUID userId = UUID.randomUUID();
    User user = user(userId, UserStatus.LOCKED);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    useCase.execute(userId);

    assertEquals(UserStatus.ACTIVE, user.getStatus());
    verify(redisTemplate).delete(LOGIN_ATTEMPTS_KEY + user.getEmail().value());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve manter usuario ativo e limpar tentativas quando usuario ja estiver ativo")
  void execute_whenUserIsAlreadyActive_thenKeepsActiveAndClearsLoginAttempts() {
    UUID userId = UUID.randomUUID();
    User user = user(userId, UserStatus.ACTIVE);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    useCase.execute(userId);

    assertEquals(UserStatus.ACTIVE, user.getStatus());
    verify(redisTemplate).delete(LOGIN_ATTEMPTS_KEY + user.getEmail().value());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve lancar UserNotFoundException quando usuario nao existir")
  void execute_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> useCase.execute(userId));
  }

  private User user(UUID id, UserStatus status) {
    return User.builder()
        .id(id)
        .name("Locked User")
        .email(new Email("locked@example.com"))
        .password(new Password("password123"))
        .roles(Set.of(UserRole.USER))
        .status(status)
        .active(true)
        .build();
  }
}
