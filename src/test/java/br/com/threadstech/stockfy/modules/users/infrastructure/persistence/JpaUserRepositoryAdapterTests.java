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

package br.com.threadstech.stockfy.modules.users.infrastructure.persistence;

import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.infrastructure.persistence.JpaUserRepositoryAdapter;
import br.com.threadstech.stockfy.users.infrastructure.persistence.SpringDataUserRepository;
import br.com.threadstech.stockfy.users.infrastructure.persistence.UserJpaEntity;
import br.com.threadstech.stockfy.users.infrastructure.persistence.UserPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

class JpaUserRepositoryAdapterTests {

	private final SpringDataUserRepository repository = org.mockito.Mockito.mock(SpringDataUserRepository.class);

	private final UserPersistenceMapper mapper = org.mockito.Mockito.mock(UserPersistenceMapper.class);

	private final JpaUserRepositoryAdapter adapter = new JpaUserRepositoryAdapter(this.repository, this.mapper);

	@Test
	@DisplayName("Deve traduzir violacao unica de email para EmailAlreadyExistsException ao salvar")
	void save_whenEmailUniqueConstraintFails_thenThrowsEmailAlreadyExistsException() {
		User user = user();
		given(this.mapper.toEntity(user)).willReturn(new UserJpaEntity());
		given(this.repository.saveAndFlush(any(UserJpaEntity.class))).willThrow(new DataIntegrityViolationException(
				"duplicate key value violates unique constraint \"users_email_key\""));

		assertThatExceptionOfType(EmailAlreadyExistsException.class).isThrownBy(() -> this.adapter.save(user));
	}

	@Test
	@DisplayName("Deve traduzir violacao unica de email para EmailAlreadyExistsException ao atualizar")
	void update_whenEmailUniqueConstraintFails_thenThrowsEmailAlreadyExistsException() {
		User user = user();
		given(this.mapper.toEntity(user)).willReturn(new UserJpaEntity());
		given(this.repository.saveAndFlush(any(UserJpaEntity.class))).willThrow(new DataIntegrityViolationException(
				"duplicate key value violates unique constraint \"users_email_key\""));

		assertThatExceptionOfType(EmailAlreadyExistsException.class).isThrownBy(() -> this.adapter.update(user));
	}

	private User user() {
		return User.builder()
			.id(UUID.randomUUID())
			.name("John Doe")
			.email(new Email("john@example.com"))
			.password(new Password("hashed-password"))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
