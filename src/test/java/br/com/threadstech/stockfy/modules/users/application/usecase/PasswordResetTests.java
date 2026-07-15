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

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.application.usecase.GenerateResetCodeUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ResetCodeHasher resetCodeHasher;

	@InjectMocks
	private GenerateResetCodeUseCase generateResetCodeUseCase;

	private User user;

	private final UUID userId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		this.user = this.user.builder()
			.id(this.userId)
			.name("John Doe")
			.email(new Email("john@example.com"))
			.password(new Password("hashed_password"))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

	@Test
	@DisplayName("Deve gerar codigo de seis digitos, persistir hash e expiracao")
	void generateResetCode_whenUserExists_thenPersistsHashedCodeAndExpiration() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));
		given(this.resetCodeHasher.hash(any())).willReturn("hashed_code");

		String code = this.generateResetCodeUseCase.execute(this.userId);

		assertThat(code.matches("\\d{6}")).isTrue();
		assertThat(this.user.getResetPasswordCodeHash()).isEqualTo("hashed_code");
		assertThat(this.user.getResetPasswordExpiresAt()).isNotNull();
		assertThat(this.user.getResetPasswordExpiresAt().isAfter(LocalDateTime.now())).isTrue();
		verify(this.userRepository).update(this.user);
	}

	@Test
	@DisplayName("Deve lancar UserNotFoundException quando usuario nao existir")
	void generateResetCode_whenUserDoesNotExist_thenThrowsUserNotFoundException() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.empty());

		assertThatExceptionOfType(UserNotFoundException.class)
			.isThrownBy(() -> this.generateResetCodeUseCase.execute(this.userId));
		verify(this.userRepository, never()).update(any());
	}

}
