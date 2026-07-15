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

package br.com.threadstech.stockfy.modules.users.application.usecase;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.UserListItemResponse;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.usecase.FindUserByIdUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class FindUserByIdUseCaseTests {

	private final UserRepository userRepository = mock(UserRepository.class);

	private final FindUserByIdUseCase useCase = new FindUserByIdUseCase(this.userRepository);

	@Test
	@DisplayName("Deve retornar detalhes quando usuario existir")
	void execute_whenUserExists_thenReturnsDetailedResponse() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, UserRole.USER);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));

		UserListItemResponse response = this.useCase.execute(userId);

		assertThat(response.name()).isEqualTo("Owner");
		assertThat(response.email()).isEqualTo("owner@example.com");
		assertThat(response.roles()).isEqualTo(Set.of(UserRole.USER));
		assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	@DisplayName("Deve lançar excecao quando usuario nao existir")
	void execute_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
		UUID requestedId = UUID.randomUUID();
		given(this.userRepository.findById(requestedId)).willReturn(Optional.empty());

		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> this.useCase.execute(requestedId));
	}

	private User user(UUID id, UserRole role) {
		return User.builder()
			.id(id)
			.name("Owner")
			.email(new Email("owner@example.com"))
			.password(new Password("password123"))
			.roles(Set.of(role))
			.status(UserStatus.ACTIVE)
			.active(true)
			.createdAt(LocalDateTime.of(2026, 4, 28, 10, 0))
			.createdBy(UUID.randomUUID())
			.updatedAt(LocalDateTime.of(2026, 4, 28, 11, 0))
			.updatedBy(UUID.randomUUID())
			.build();
	}

}
