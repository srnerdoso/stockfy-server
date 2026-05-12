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

package br.com.threadstech.stockfy.modules.users.application.validation;

import br.com.threadstech.stockfy.users.application.validation.ValidUserRoleValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidUserRoleValidatorTests {

	private final ValidUserRoleValidator validator = new ValidUserRoleValidator();

	@Test
	@DisplayName("Deve aceitar role valida existente no enum UserRole")
	void isValid_whenRoleExistsInEnum_thenReturnsTrue() {
		assertThat(this.validator.isValid("ADMIN", null)).isTrue();
	}

	@Test
	@DisplayName("Deve rejeitar role invalida quando nao existir no enum UserRole")
	void isValid_whenRoleDoesNotExistInEnum_thenReturnsFalse() {
		assertThat(this.validator.isValid("INVALID", null)).isFalse();
	}

	@Test
	@DisplayName("Deve rejeitar role ausente quando valor for vazio")
	void isValid_whenRoleIsBlank_thenReturnsFalse() {
		assertThat(this.validator.isValid("", null)).isFalse();
	}

	@Test
	@DisplayName("Deve aceitar valor nulo para delegar validacao ao NotNull")
	void isValid_whenRoleIsNull_thenReturnsTrue() {
		assertThat(this.validator.isValid(null, null)).isTrue();
	}

}
