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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.CurrentPasswordInvalidException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordResetCodeException;
import br.com.threadstech.stockfy.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.application.usecase.UpdatePasswordUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordUseCaseTests {

	private static final String RESET_CODE = "123456";

	private static final String RESET_CODE_HASH = String.join("-", "reset", "code", "hash");

	private static final String NEW_PASSWORD = "newPassword123";

	private static final String NEW_PASSWORD_HASH = String.join("-", "new", "password", "hash");

	private static final String CURRENT_PASSWORD = "currentPassword123";

	private static final String CURRENT_PASSWORD_HASH = String.join("-", "current", "password", "hash");

	private static final String USER_NAME = "John Doe";

	private static final String USER_EMAIL = "john@example.com";

	private static final String WRONG_PASSWORD = "wrongPassword";

	private static final String INVALID_ATTEMPT_KEY_PREFIX = "password_update_attempts:";

	private static final int MAX_INVALID_ATTEMPTS = 15;

	private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);

	private final PasswordEncoder passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);

	private final ResetCodeHasher resetCodeHasher = org.mockito.Mockito.mock(ResetCodeHasher.class);

	private final StringRedisTemplate redisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);

	@Mock
	private ValueOperations<String, String> valueOperations;

	private final UpdatePasswordUseCase useCase = new UpdatePasswordUseCase(this.userRepository, this.passwordEncoder,
			this.resetCodeHasher, this.redisTemplate);

	@Test
	@DisplayName("Deve atualizar senha e revogar codigo quando codigo for valido")
	void executeWithCode_whenCodeIsValid_thenUpdatesPasswordAndRevokesCode() {
		User user = user(UUID.randomUUID());
		user.setResetPasswordCodeHash(RESET_CODE_HASH);
		user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(10));
		given(this.resetCodeHasher.hash(RESET_CODE)).willReturn(RESET_CODE_HASH);
		given(this.userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).willReturn(Optional.of(user));
		given(this.passwordEncoder.encode(NEW_PASSWORD)).willReturn(NEW_PASSWORD_HASH);

		this.useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD);

		assertThat(user.getPassword().value()).isEqualTo(NEW_PASSWORD_HASH);
		assertThat(user.getResetPasswordCodeHash()).isNull();
		assertThat(user.getResetPasswordExpiresAt()).isNull();
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve lancar InvalidPasswordResetCodeException quando codigo for invalido")
	void executeWithCode_whenCodeIsInvalid_thenThrowsInvalidPasswordResetCodeException() {
		given(this.resetCodeHasher.hash(RESET_CODE)).willReturn(RESET_CODE_HASH);
		given(this.userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).willReturn(Optional.empty());

		assertThatExceptionOfType(InvalidPasswordResetCodeException.class)
			.isThrownBy(() -> this.useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD));

		verify(this.userRepository, never()).update(any());
	}

	@Test
	@DisplayName("Deve lancar InvalidPasswordResetCodeException quando codigo estiver expirado")
	void executeWithCode_whenCodeIsExpired_thenThrowsInvalidPasswordResetCodeException() {
		User user = user(UUID.randomUUID());
		user.setResetPasswordCodeHash(RESET_CODE_HASH);
		user.setResetPasswordExpiresAt(LocalDateTime.now().minusMinutes(1));
		given(this.resetCodeHasher.hash(RESET_CODE)).willReturn(RESET_CODE_HASH);
		given(this.userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).willReturn(Optional.of(user));

		assertThatExceptionOfType(InvalidPasswordResetCodeException.class)
			.isThrownBy(() -> this.useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD));

		verify(this.userRepository, never()).update(any());
	}

	@Test
	@DisplayName("Deve buscar usuario pelo hash do codigo sem varrer usuarios")
	void executeWithCode_whenCalled_thenFindsUserByResetCodeHashWithoutScanningUsers() {
		User user = user(UUID.randomUUID());
		user.setResetPasswordCodeHash(RESET_CODE_HASH);
		user.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(10));
		given(this.resetCodeHasher.hash(RESET_CODE)).willReturn(RESET_CODE_HASH);
		given(this.userRepository.findByResetPasswordCodeHash(RESET_CODE_HASH)).willReturn(Optional.of(user));
		given(this.passwordEncoder.encode(NEW_PASSWORD)).willReturn(NEW_PASSWORD_HASH);

		this.useCase.executeWithCode(RESET_CODE, NEW_PASSWORD, NEW_PASSWORD);

		verify(this.userRepository).findByResetPasswordCodeHash(RESET_CODE_HASH);
		verify(this.userRepository, never()).findAll(any());
	}

	@Test
	@DisplayName("Deve atualizar senha quando senha atual conferir")
	void executeAuthenticated_whenCurrentPasswordMatches_thenUpdatesPassword() {
		UUID userId = UUID.randomUUID();
		User user = user(userId);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_PASSWORD_HASH)).willReturn(true);
		given(this.passwordEncoder.encode(NEW_PASSWORD)).willReturn(NEW_PASSWORD_HASH);

		this.useCase.executeAuthenticated(userId, CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);

		assertThat(user.getPassword().value()).isEqualTo(NEW_PASSWORD_HASH);
		verify(this.redisTemplate).delete(INVALID_ATTEMPT_KEY_PREFIX + userId);
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve lancar CurrentPasswordInvalidException e contar tentativa quando senha atual falhar")
	void executeAuthenticated_whenCurrentPasswordIsWrong_thenThrowsCurrentPasswordInvalidExceptionAndCountsAttempt() {
		UUID userId = UUID.randomUUID();
		User user = user(userId);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.passwordEncoder.matches(WRONG_PASSWORD, CURRENT_PASSWORD_HASH)).willReturn(false);
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		given(this.valueOperations.increment(INVALID_ATTEMPT_KEY_PREFIX + userId)).willReturn(1L);

		assertThatExceptionOfType(CurrentPasswordInvalidException.class)
			.isThrownBy(() -> this.useCase.executeAuthenticated(userId, WRONG_PASSWORD, NEW_PASSWORD, NEW_PASSWORD));

		verify(this.valueOperations).increment(INVALID_ATTEMPT_KEY_PREFIX + userId);
		verify(this.redisTemplate).expire(INVALID_ATTEMPT_KEY_PREFIX + userId, Duration.ofMinutes(5));
		verify(this.userRepository, never()).update(user);
	}

	@Test
	@DisplayName("Deve lancar CurrentPasswordInvalidException quando senha atual for omitida")
	void executeAuthenticated_whenCurrentPasswordIsMissing_thenThrowsCurrentPasswordInvalidException() {
		UUID userId = UUID.randomUUID();
		User user = user(userId);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		given(this.valueOperations.increment(INVALID_ATTEMPT_KEY_PREFIX + userId)).willReturn(1L);

		assertThatExceptionOfType(CurrentPasswordInvalidException.class)
			.isThrownBy(() -> this.useCase.executeAuthenticated(userId, null, NEW_PASSWORD, NEW_PASSWORD));

		verify(this.passwordEncoder, never()).matches(any(), any());
		verify(this.valueOperations).increment(INVALID_ATTEMPT_KEY_PREFIX + userId);
		verify(this.userRepository, never()).update(user);
	}

	@Test
	@DisplayName("Deve bloquear usuario na decima quinta tentativa invalida")
	void executeAuthenticated_whenFifteenthInvalidAttempt_thenLocksUser() {
		UUID userId = UUID.randomUUID();
		User user = user(userId);
		given(this.userRepository.findById(userId)).willReturn(Optional.of(user));
		given(this.passwordEncoder.matches(WRONG_PASSWORD, CURRENT_PASSWORD_HASH)).willReturn(false);
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		given(this.valueOperations.increment(INVALID_ATTEMPT_KEY_PREFIX + userId))
			.willReturn((long) MAX_INVALID_ATTEMPTS);

		assertThatExceptionOfType(CurrentPasswordInvalidException.class)
			.isThrownBy(() -> this.useCase.executeAuthenticated(userId, WRONG_PASSWORD, NEW_PASSWORD, NEW_PASSWORD));

		assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
		verify(this.userRepository).update(user);
	}

	@Test
	@DisplayName("Deve lancar PasswordMismatchException quando confirmacao nao conferir")
	void execute_whenPasswordsDoNotMatch_thenThrowsPasswordMismatchException() {
		UUID userId = UUID.randomUUID();

		assertThatExceptionOfType(PasswordMismatchException.class).isThrownBy(
				() -> this.useCase.executeAuthenticated(userId, CURRENT_PASSWORD, NEW_PASSWORD, "differentPassword"));

		verifyNoInteractions(this.userRepository, this.passwordEncoder, this.resetCodeHasher, this.redisTemplate);
	}

	private User user(UUID id) {
		return User.builder()
			.id(id)
			.name(USER_NAME)
			.email(new Email(USER_EMAIL))
			.password(new Password(CURRENT_PASSWORD_HASH))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
