package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private GenerateResetCodeUseCase generateResetCodeUseCase;
  @InjectMocks private ResetPasswordUseCase resetPasswordUseCase;

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
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
  }

  @Test
  @DisplayName("Should generate reset code for user")
  void shouldGenerateResetCode() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(any())).thenReturn("hashed_code");

    String code = generateResetCodeUseCase.execute(userId);

    assertNotNull(code);
    verify(userRepository).update(any());
  }

  @Test
  @DisplayName("Should reset password with valid code")
  void shouldResetPassword() {
    // Prepare user with reset code
    user =
        User.builder()
            .id(userId)
            .name("John Doe")
            .email(new Email("john@example.com"))
            .password(new Password("old_hash"))
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .active(true)
            .resetPasswordCodeHash("hashed_code")
            .resetPasswordExpiresAt(LocalDateTime.now().plusHours(1))
            .build();

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq("123456"), any())).thenReturn(true);
    when(passwordEncoder.encode(eq("new_password"))).thenReturn("new_hash");

    resetPasswordUseCase.execute("john@example.com", "123456", "new_password");

    assertEquals("new_hash", user.getPassword().value());
    assertNull(user.getResetPasswordCodeHash());
    verify(userRepository).update(user);
  }
}
