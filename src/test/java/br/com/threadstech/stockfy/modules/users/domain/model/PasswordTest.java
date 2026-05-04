package br.com.threadstech.stockfy.modules.users.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.users.domain.model.Password;
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
		assertThrows(InvalidPasswordException.class, () -> new Password("short"));
	}

	@Test
	@DisplayName("Should throw exception for null or empty password")
	void shouldThrowExceptionForNullOrEmptyPassword() {
		assertThrows(InvalidPasswordException.class, () -> new Password(null));
		assertThrows(InvalidPasswordException.class, () -> new Password(""));
	}

}
