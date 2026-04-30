package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.Optional;
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
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();
  }

  @Test
  @DisplayName("Should generate reset code for user")
  void shouldGenerateResetCode() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(resetCodeHasher.hash(any())).thenReturn("hashed_code");

    String code = generateResetCodeUseCase.execute(userId);

    assertNotNull(code);
    assertEquals("hashed_code", user.getResetPasswordCodeHash());
    verify(userRepository).update(any());
  }
}
