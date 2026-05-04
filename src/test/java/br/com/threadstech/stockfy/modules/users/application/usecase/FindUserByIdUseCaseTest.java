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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindUserByIdUseCaseTest {

	private final UserRepository userRepository = mock(UserRepository.class);

	private final FindUserByIdUseCase useCase = new FindUserByIdUseCase(userRepository);

	@Test
	@DisplayName("Deve retornar detalhes quando usuario existir")
	void execute_whenUserExists_thenReturnsDetailedResponse() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, UserRole.USER);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		UserListItemResponse response = useCase.execute(userId);

		assertEquals("Owner", response.name());
		assertEquals("owner@example.com", response.email());
		assertEquals(Set.of(UserRole.USER), response.roles());
		assertEquals(UserStatus.ACTIVE, response.status());
	}

	@Test
	@DisplayName("Deve lançar excecao quando usuario nao existir")
	void execute_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
		UUID requestedId = UUID.randomUUID();
		when(userRepository.findById(requestedId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> useCase.execute(requestedId));
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
