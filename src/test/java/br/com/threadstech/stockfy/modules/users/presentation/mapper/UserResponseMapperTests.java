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

package br.com.threadstech.stockfy.modules.users.presentation.mapper;

import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.presentation.mapper.UserResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseMapperTests {

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private static final String USER_NAME = "John";

	private final UserResponseMapper mapper = Mappers.getMapper(UserResponseMapper.class);

	@Test
	@DisplayName("Deve mapear resposta completa com campos sensiveis para contexto permitido")
	void toFullResponse_whenCalled_thenMapsAllFieldsFromUser() {
		User user = user();

		UserResponse response = this.mapper.toFullResponse(user);

		assertThat(response.id()).isEqualTo(USER_ID);
		assertThat(response.name()).isEqualTo(USER_NAME);
		assertThat(response.email()).isEqualTo("john@example.com");
		assertThat(response.roles()).containsExactlyInAnyOrder(UserRole.USER);
	}

	@Test
	@DisplayName("Deve mapear resposta de owner com email e roles")
	void toOwnerResponse_whenCalled_thenMapsOwnerFields() {
		User user = user();

		UserResponse response = this.mapper.toOwnerResponse(user);

		assertThat(response.id()).isEqualTo(USER_ID);
		assertThat(response.name()).isEqualTo(USER_NAME);
		assertThat(response.email()).isEqualTo("john@example.com");
		assertThat(response.roles()).containsExactlyInAnyOrder(UserRole.USER);
	}

	@Test
	@DisplayName("Deve mapear resposta publica sem email e com roles vazio")
	void toPublicResponse_whenCalled_thenMapsPublicFieldsWithEmptyRoles() {
		User user = user();

		UserResponse response = this.mapper.toPublicResponse(user);

		assertThat(response.id()).isEqualTo(USER_ID);
		assertThat(response.name()).isEqualTo(USER_NAME);
		assertThat(response.email()).isNull();
		assertThat(response.roles()).isEmpty();
	}

	private User user() {
		return User.builder()
			.id(USER_ID)
			.name(USER_NAME)
			.email(new Email("john@example.com"))
			.roles(Set.of(UserRole.USER))
			.build();
	}

}
