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

package br.com.threadstech.stockfy.modules.users.application.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTests {

	@Test
	@DisplayName("Deve manter roles vazio quando resposta for criada com roles nulo")
	void constructor_whenRolesIsNull_thenKeepsEmptyRolesWithoutThrowing() {
		UserResponse response = UserResponse.builder().id(UUID.randomUUID()).name("John").roles(null).build();

		assertThat(response.roles()).isEmpty();
	}

	@Test
	@DisplayName("Deve proteger colecao de roles contra mutacao externa")
	void roles_whenResponseHasRoles_thenReturnsDefensiveCopy() {
		Set<UserRole> roles = new HashSet<>();
		roles.add(UserRole.USER);

		UserResponse response = UserResponse.builder().id(UUID.randomUUID()).name("John").roles(roles).build();

		roles.add(UserRole.ADMIN);

		assertThat(response.roles()).containsExactly(UserRole.USER);
	}

}
