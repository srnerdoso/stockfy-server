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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UpdateProfileUseCaseTests {

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);

	private final UpdateProfileUseCase useCase = new UpdateProfileUseCase(this.userRepository);

	@Test
	@DisplayName("Deve atualizar nome e email quando dados forem validos")
	void execute_whenNameAndEmailAreProvided_thenUpdatesUser() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.userRepository.findByEmail(new Email("new@example.com"))).willReturn(Optional.empty());

		this.useCase.execute(userId, "New Name", "new@example.com");

		assertThat(user.getName()).isEqualTo("New Name");
		assertThat(user.getEmail().value()).isEqualTo("new@example.com");
		assertThat(user.getRoles()).isEqualTo(Set.of(UserRole.USER));
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve preservar email atual quando email for omitido")
	void execute_whenEmailIsNull_thenKeepsCurrentEmail() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));

		this.useCase.execute(userId, "New Name", null);

		assertThat(user.getName()).isEqualTo("New Name");
		assertThat(user.getEmail().value()).isEqualTo("old@example.com");
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve preservar nome atual quando nome for omitido")
	void execute_whenNameIsNull_thenKeepsCurrentName() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.userRepository.findByEmail(new Email("new@example.com"))).willReturn(Optional.empty());

		this.useCase.execute(userId, null, "new@example.com");

		assertThat(user.getName()).isEqualTo("Old Name");
		assertThat(user.getEmail().value()).isEqualTo("new@example.com");
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve permitir manter o mesmo email sem conflito")
	void execute_whenEmailIsCurrentUserEmail_thenDoesNotThrowConflict() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));

		this.useCase.execute(userId, "New Name", "old@example.com");

		assertThat(user.getName()).isEqualTo("New Name");
		assertThat(user.getEmail().value()).isEqualTo("old@example.com");
		verify(this.userRepository, never()).findByEmail(new Email("old@example.com"));
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve lancar conflito quando email pertencer a outro usuario")
	void execute_whenEmailBelongsToAnotherUser_thenThrowsEmailAlreadyExistsException() {
		UUID userId = UUID.randomUUID();
		UUID anotherUserId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		User anotherUser = user(anotherUserId, "Another", "taken@example.com");
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.userRepository.findByEmail(new Email("taken@example.com"))).willReturn(Optional.of(anotherUser));

		assertThatExceptionOfType(EmailAlreadyExistsException.class)
			.isThrownBy(() -> this.useCase.execute(userId, "New Name", "taken@example.com"));
		assertThat(user.getName()).isEqualTo("Old Name");
		assertThat(user.getEmail().value()).isEqualTo("old@example.com");
		verify(this.userRepository, never()).update(user);
	}

	@Test
	@DisplayName("Nao deve persistir quando nome e email forem omitidos")
	void execute_whenNameAndEmailAreNull_thenDoesNotUpdateUser() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, "Old Name", "old@example.com");
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));

		this.useCase.execute(userId, null, null);

		assertThat(user.getName()).isEqualTo("Old Name");
		assertThat(user.getEmail().value()).isEqualTo("old@example.com");
		verify(this.userRepository, never()).update(user);
	}

	@Test
	@DisplayName("Deve lancar UserNotFoundException quando usuario autenticado nao existir")
	void execute_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
		UUID userId = UUID.randomUUID();
		given(this.userRepository.findById(userId)).willReturn(Optional.empty());

		assertThatExceptionOfType(UserNotFoundException.class)
			.isThrownBy(() -> this.useCase.execute(userId, "New Name", "new@example.com"));
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
