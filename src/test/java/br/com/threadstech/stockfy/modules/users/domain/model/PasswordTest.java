package br.com.threadstech.stockfy.modules.users.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasswordTest {

  @Test
  @DisplayName("Should create password with valid length")
  void shouldCreatePasswordWithValidLength() {
    String validPass = "password123";
    Password password = new Password(validPass);
    assertEquals(validPass, password.value());
  }

  @Test
  @DisplayName("Should throw exception for short password")
  void shouldThrowExceptionForShortPassword() {
    assertThrows(IllegalArgumentException.class, () -> new Password("short"));
  }

  @Test
  @DisplayName("Should throw exception for null or empty password")
  void shouldThrowExceptionForNullOrEmptyPassword() {
    assertThrows(IllegalArgumentException.class, () -> new Password(null));
    assertThrows(IllegalArgumentException.class, () -> new Password(""));
  }
}
