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

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.usecase.UnlockUserUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class UnlockUserUseCaseTests {

	private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);

	private final StringRedisTemplate redisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);

	private final UnlockUserUseCase useCase = new UnlockUserUseCase(this.userRepository, this.redisTemplate);

	@Test
	@DisplayName("Deve desbloquear usuario bloqueado e limpar tentativas de login")
	void execute_whenUserIsLocked_thenUnlocksUserAndClearsLoginAttempts() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, UserStatus.LOCKED);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));

		this.useCase.execute(userId);

		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		verify(this.redisTemplate).delete(LOGIN_ATTEMPTS_KEY + user.getEmail().value());
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve manter usuario ativo e limpar tentativas quando usuario ja estiver ativo")
	void execute_whenUserIsAlreadyActive_thenKeepsActiveAndClearsLoginAttempts() {
		UUID userId = UUID.randomUUID();
		User user = user(userId, UserStatus.ACTIVE);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));

		this.useCase.execute(userId);

		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
		verify(this.redisTemplate).delete(LOGIN_ATTEMPTS_KEY + user.getEmail().value());
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve lancar UserNotFoundException quando usuario nao existir")
	void execute_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
		UUID userId = UUID.randomUUID();
		given(this.userRepository.findById(userId)).willReturn(Optional.empty());

		assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> this.useCase.execute(userId));
	}

	private User user(UUID id, UserStatus status) {
		return User.builder()
			.id(id)
			.name("Locked User")
			.email(new Email("locked@example.com"))
			.password(new Password("password123"))
			.roles(Set.of(UserRole.USER))
			.status(status)
			.active(true)
			.build();
	}

}
