package br.com.threadstech.stockfy.modules.users.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.threadstech.stockfy.users.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

	@Test
	@DisplayName("Should create email with valid address")
	void shouldCreateEmailWithValidAddress() {
		String validAddress = "test@example.com";
		Email email = new Email(validAddress);
		assertEquals(validAddress, email.value());
	}

	@ParameterizedTest
	@ValueSource(strings = { "invalid-email", "test@", "@example.com", "test@example", "" })
	@DisplayName("Should throw exception for invalid email formats")
	void shouldThrowExceptionForInvalidEmailFormats(String invalidAddress) {
		assertThrows(IllegalArgumentException.class, () -> new Email(invalidAddress));
	}

	@Test
	@DisplayName("Should throw exception for null email")
	void shouldThrowExceptionForNullEmail() {
		assertThrows(IllegalArgumentException.class, () -> new Email(null));
	}

}
