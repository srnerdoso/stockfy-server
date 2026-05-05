/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.threadstech.stockfy.modules.users.domain.model;

import br.com.threadstech.stockfy.users.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailTests {

	@Test
	@DisplayName("Should create email with valid address")
	void shouldCreateEmailWithValidAddress() {
		String validAddress = "test@example.com";
		Email email = new Email(validAddress);
		assertThat(email.value()).isEqualTo(validAddress);
	}

	@ParameterizedTest
	@ValueSource(strings = { "invalid-email", "test@", "@example.com", "test@example", "" })
	@DisplayName("Should throw exception for invalid email formats")
	void shouldThrowExceptionForInvalidEmailFormats(String invalidAddress) {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Email(invalidAddress));
	}

	@Test
	@DisplayName("Should throw exception for null email")
	void shouldThrowExceptionForNullEmail() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Email(null));
	}

}
