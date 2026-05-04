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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.usecase.UpdateProfileUseCase;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProfileUseCaseTest {

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);

	private final UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

	@Test
	@DisplayName("Deve atualizar nome e email quando dados forem validos")
	void execute_whenNameAndEmailAreProvided_thenUpdatesUser() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findByEmail(new Email("new@example.com"))).thenReturn(Optional.empty());

		useCase.execute(userId, "New Name", "new@example.com");

		assertEquals("New Name", user.getName());
		assertEquals("new@example.com", user.getEmail().value());
		assertEquals(Set.of(UserRole.USER), user.getRoles());
		assertEquals(UserStatus.ACTIVE, user.getStatus());
		verify(userRepository).update(user);
	}

	@Test
	@DisplayName("Deve preservar email atual quando email for omitido")
	void execute_whenEmailIsNull_thenKeepsCurrentEmail() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		useCase.execute(userId, "New Name", null);

		assertEquals("New Name", user.getName());
		assertEquals("old@example.com", user.getEmail().value());
		verify(userRepository).update(user);
	}

	@Test
	@DisplayName("Deve preservar nome atual quando nome for omitido")
	void execute_whenNameIsNull_thenKeepsCurrentName() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findByEmail(new Email("new@example.com"))).thenReturn(Optional.empty());

		useCase.execute(userId, null, "new@example.com");

		assertEquals("Old Name", user.getName());
		assertEquals("new@example.com", user.getEmail().value());
		verify(userRepository).update(user);
	}

	@Test
	@DisplayName("Deve permitir manter o mesmo email sem conflito")
	void execute_whenEmailIsCurrentUserEmail_thenDoesNotThrowConflict() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		useCase.execute(userId, "New Name", "old@example.com");

		assertEquals("New Name", user.getName());
		assertEquals("old@example.com", user.getEmail().value());
		verify(userRepository, never()).findByEmail(new Email("old@example.com"));
		verify(userRepository).update(user);
	}

	@Test
	@DisplayName("Deve lancar conflito quando email pertencer a outro usuario")
	void execute_whenEmailBelongsToAnotherUser_thenThrowsEmailAlreadyExistsException() {
		UUID userId = UUID.randomUUID();
		UUID anotherUserId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		User anotherUser = user(anotherUserId, "Another", "taken@example.com");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findByEmail(new Email("taken@example.com"))).thenReturn(Optional.of(anotherUser));

		assertThrows(EmailAlreadyExistsException.class, () -> useCase.execute(userId, "New Name", "taken@example.com"));

		assertEquals("Old Name", user.getName());
		assertEquals("old@example.com", user.getEmail().value());
		verify(userRepository, never()).update(user);
	}

	@Test
	@DisplayName("Nao deve persistir quando nome e email forem omitidos")
	void execute_whenNameAndEmailAreNull_thenDoesNotUpdateUser() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		useCase.execute(userId, null, null);

		assertEquals("Old Name", user.getName());
		assertEquals("old@example.com", user.getEmail().value());
		verify(userRepository, never()).update(user);
	}

	@Test
	@DisplayName("Deve lancar UserNotFoundException quando usuario autenticado nao existir")
	void execute_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
		UUID userId = UUID.randomUUID();
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> useCase.execute(userId, "New Name", "new@example.com"));
	}

	private User user(UUID id, String name, String email) {
		return User.builder()
			.id(id)
			.name(name)
			.email(new Email(email))
			.password(new Password("password123"))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
