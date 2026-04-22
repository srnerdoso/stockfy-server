package br.com.threadstech.stockfy.modules.users.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("Should create active user by default")
  void shouldCreateActiveUserByDefault() {
    User user = createUser();
    assertTrue(user.isActive());
    assertEquals(UserStatus.ACTIVE, user.getStatus());
  }

  @Test
  @DisplayName("Should lock user account")
  void shouldLockUserAccount() {
    User user = createUser();
    user.lock();
    assertEquals(UserStatus.LOCKED, user.getStatus());
  }

  @Test
  @DisplayName("Should unlock user account")
  void shouldUnlockUserAccount() {
    User user = createUser();
    user.lock();
    user.unlock();
    assertEquals(UserStatus.ACTIVE, user.getStatus());
  }

  @Test
  @DisplayName("Should deactivate user (soft delete)")
  void shouldDeactivateUser() {
    User user = createUser();
    user.deactivate();
    assertFalse(user.isActive());
  }

  private User createUser() {
    return User.builder()
        .id(UUID.randomUUID())
        .name("John Doe")
        .email(new Email("john@example.com"))
        .password(new Password("password123"))
        .role(UserRole.USER)
        .status(UserStatus.ACTIVE)
        .active(true)
        .build();
  }
}
