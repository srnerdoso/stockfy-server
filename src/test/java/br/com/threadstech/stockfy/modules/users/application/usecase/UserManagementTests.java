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

import br.com.threadstech.stockfy.users.application.dto.RegisterUserRequest;
import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.application.usecase.DeleteUserUseCase;
import br.com.threadstech.stockfy.users.application.usecase.RegisterUserUseCase;
import br.com.threadstech.stockfy.users.application.usecase.UpdateProfileUseCase;
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

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserManagementTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private RegisterUserUseCase registerUserUseCase;

	@InjectMocks
	private UpdateProfileUseCase updateProfileUseCase;

	@InjectMocks
	private DeleteUserUseCase deleteUserUseCase;

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
	@DisplayName("Should register new user")
	void shouldRegisterNewUser() {
		given(this.userRepository.findByEmail(any())).willReturn(Optional.empty());
		given(this.passwordEncoder.encode(any())).willReturn("hashed_password");

		var request = new RegisterUserRequest("New User", "new@example.com", "password123", null, UserRole.USER);
		this.registerUserUseCase.execute(request);

		verify(this.userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("Should throw exception if email already exists")
	void shouldThrowExceptionIfEmailExists() {
		given(this.userRepository.findByEmail(any())).willReturn(Optional.of(this.user));

		var request = new RegisterUserRequest("John", "john@example.com", "pass", null, UserRole.USER);
		assertThatExceptionOfType(EmailAlreadyExistsException.class)
			.isThrownBy(() -> this.registerUserUseCase.execute(request));
	}

	@Test
	@DisplayName("Should update user profile")
	void shouldUpdateUserProfile() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		this.updateProfileUseCase.execute(this.userId, "Updated Name", "updated@example.com");

		assertThat(this.user.getName()).isEqualTo("Updated Name");
		assertThat(this.user.getEmail().value()).isEqualTo("updated@example.com");
		verify(this.userRepository).update(this.user);
	}

	@Test
	@DisplayName("Should delete user (soft delete)")
	void shouldDeleteUser() {
		this.deleteUserUseCase.execute(this.userId);

		verify(this.userRepository).deleteById(this.userId);
	}

}
