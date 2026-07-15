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

import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.users.domain.model.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PasswordTests {

	@Test
	@DisplayName("Should create password with valid length")
	void shouldCreatePasswordWithValidLength() {
		String validPass = "password123";
		Password password = new Password(validPass);
		assertThat(password.value()).isEqualTo(validPass);
	}

	@Test
	@DisplayName("Should throw exception for short password")
	void shouldThrowExceptionForShortPassword() {
		assertThatExceptionOfType(InvalidPasswordException.class).isThrownBy(() -> new Password("short"));
	}

	@Test
	@DisplayName("Should throw exception for null or empty password")
	void shouldThrowExceptionForNullOrEmptyPassword() {
		assertThatExceptionOfType(InvalidPasswordException.class).isThrownBy(() -> new Password(null));
		assertThatExceptionOfType(InvalidPasswordException.class).isThrownBy(() -> new Password(""));
	}

}
