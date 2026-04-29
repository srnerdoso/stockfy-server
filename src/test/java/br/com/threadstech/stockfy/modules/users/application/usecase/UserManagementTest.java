package br.com.threadstech.stockfy.modules.users.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.users.application.dto.RegisterUserRequest;
import br.com.threadstech.stockfy.modules.users.application.exception.EmailAlreadyExistsException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserManagementTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private RegisterUserUseCase registerUserUseCase;
  @InjectMocks private UpdateProfileUseCase updateProfileUseCase;
  @InjectMocks private DeleteUserUseCase deleteUserUseCase;

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
  @DisplayName("Should register new user")
  void shouldRegisterNewUser() {
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(any())).thenReturn("hashed_password");

    var request =
        new RegisterUserRequest("New User", "new@example.com", "password123", null, UserRole.USER);
    registerUserUseCase.execute(request);

    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw exception if email already exists")
  void shouldThrowExceptionIfEmailExists() {
    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

    var request = new RegisterUserRequest("John", "john@example.com", "pass", null, UserRole.USER);
    assertThrows(EmailAlreadyExistsException.class, () -> registerUserUseCase.execute(request));
  }

  @Test
  @DisplayName("Should update user profile")
  void shouldUpdateUserProfile() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    updateProfileUseCase.execute(userId, "Updated Name", "updated@example.com");

    assertEquals("Updated Name", user.getName());
    assertEquals("updated@example.com", user.getEmail().value());
    verify(userRepository).update(user);
  }

  @Test
  @DisplayName("Should delete user (soft delete)")
  void shouldDeleteUser() {
    deleteUserUseCase.execute(userId);

    verify(userRepository).deleteById(userId);
  }
}
