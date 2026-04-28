package br.com.threadstech.stockfy.modules.users.application.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.threadstech.stockfy.web.application.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserExceptionMessageKeyTest {

  @Test
  @DisplayName("Deve expor chaves de mensagem seguindo kebab-case e hierarquia")
  void constructor_whenExceptionIsCreated_thenUsesStructuredMessageKey() {
    assertEquals("exception.email-already-exists", new EmailAlreadyExistsException().getMessage());
    assertEquals("user.password.mismatch", new PasswordMismatchException().getMessage());
    assertEquals("user.password.invalid", new InvalidPasswordException().getMessage());
  }
}
