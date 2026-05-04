package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.application.usecase.GenerateResetCodeUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetTest {

  @Mock private UserRepository userRepository;
  @Mock private ResetCodeHasher resetCodeHasher;

  @InjectMocks private GenerateResetCodeUseCase generateResetCodeUseCase;

  private User user;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(userId)
            .name("John Doe")
            .email(new Email("john@example.com"))
            .password(new Password("hashed_password"))
            .roles(Set.of(UserRole.USER))
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();  }

  @Test
  @DisplayName("Deve gerar codigo de seis digitos, persistir hash e expiracao")
  void generateResetCode_whenUserExists_thenPersistsHashedCodeAndExpiration() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resetCodeHasher.hash(any())).thenReturn("hashed_code");

    String code = generateResetCodeUseCase.execute(userId);

    assertTrue(code.matches("\\d{6}"));
    assertEquals("hashed_code", user.getResetPasswordCodeHash());
    assertNotNull(user.getResetPasswordExpiresAt());
    assertTrue(user.getResetPasswordExpiresAt().isAfter(LocalDateTime.now()));
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Deve lancar UserNotFoundException quando usuario nao existir")
  void generateResetCode_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> generateResetCodeUseCase.execute(userId));
    verify(userRepository, never()).update(any());
  }
}
