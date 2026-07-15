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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.usecase.UpdateUserRolesUseCase;
import br.com.threadstech.stockfy.users.domain.exception.InvalidUserRolesException;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateUserRolesUseCaseTests {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UpdateUserRolesUseCase useCase;

	private UUID userId;

	private User user;

	@BeforeEach
	void setUp() {
		this.userId = UUID.randomUUID();
		this.user = this.user.builder().id(this.userId).roles(EnumSet.of(UserRole.USER)).build();
	}

	@Test
	@DisplayName("Deve adicionar role ADMIN a um usuario que possui apenas USER")
	void execute_whenAddingAdminToUser_thenAddsRole() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		this.useCase.execute(this.userId, Set.of(UserRole.ADMIN), Set.of());

		assertThat(this.user.getRoles()).isEqualTo(Set.of(UserRole.USER, UserRole.ADMIN));
		verify(this.userRepository).update(this.user);
	}

	@Test
	@DisplayName("Deve remover role USER de um usuario que possui {USER, ADMIN}")
	void execute_whenRemovingUserFromMultiRoleUser_thenRemovesRole() {
		this.user.setRoles(Set.of(UserRole.USER, UserRole.ADMIN));
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		this.useCase.execute(this.userId, Set.of(), Set.of(UserRole.USER));

		assertThat(this.user.getRoles()).isEqualTo(Set.of(UserRole.ADMIN));
		verify(this.userRepository).update(this.user);
	}

	@Test
	@DisplayName("Nao deve alterar roles quando add e remove forem vazios")
	void execute_whenEmptyRoles_thenNoChange() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		this.useCase.execute(this.userId, Set.of(), Set.of());

		assertThat(this.user.getRoles()).isEqualTo(Set.of(UserRole.USER));
		verify(this.userRepository).update(this.user);
	}

	@Test
	@DisplayName("Deve lancar excecao ao tentar remover todas as roles")
	void execute_whenRemovingAllRoles_thenThrowsException() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		assertThatExceptionOfType(InvalidUserRolesException.class)
			.isThrownBy(() -> this.useCase.execute(this.userId, Set.of(), Set.of(UserRole.USER)));
	}

	@Test
	@DisplayName("Deve lancar excecao quando usuario nao for encontrado")
	void execute_whenUserNotFound_thenThrowsException() {
		given(this.userRepository.findById(this.userId)).willReturn(Optional.empty());

		assertThatExceptionOfType(UserNotFoundException.class)
			.isThrownBy(() -> this.useCase.execute(this.userId, Set.of(UserRole.ADMIN), Set.of()));
	}

}
