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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserRepositoryIT {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("Should find users by name filter")
	void shouldFindUsersByNameFilter() {
		this.userRepository.save(createUser("Alice", "alice@example.com"));
		this.userRepository.save(createUser("Bob", "bob@example.com"));

		var results = this.userRepository.findAll("Ali");
		assertThat(results.size()).isEqualTo(1);
		assertThat(results.get(0).getName()).isEqualTo("Alice");
	}

	@Test
	@DisplayName("Should update user status")
	void shouldUpdateUserStatus() {
		User user = createUser("Status Test", "status@example.com");
		this.userRepository.save(user);

		user.lock();
		this.userRepository.update(user);

		var updated = this.userRepository.findById(user.getId());
		assertThat(updated.isPresent()).isTrue();
		assertThat(updated.get().getStatus()).isEqualTo(UserStatus.LOCKED);
	}

	@Test
	@DisplayName("Deve persistir roles ao salvar usuario")
	void save_whenUserHasRoles_thenPersistsRoles() {
		User user = createUser("Admin User", "admin-role@example.com", Set.of(UserRole.ADMIN, UserRole.USER));
		this.userRepository.save(user);

		var persisted = this.userRepository.findByEmail(new Email("admin-role@example.com"));
		List<String> persistedRoles = this.jdbcTemplate
			.queryForList("SELECT role FROM users_roles WHERE user_id = ? ORDER BY role", String.class, user.getId());

		assertThat(persisted.isPresent()).isTrue();
		assertThat(persisted.get().getRoles()).isEqualTo(Set.of(UserRole.ADMIN, UserRole.USER));
		assertThat(persistedRoles).isEqualTo(List.of("ADMIN", "USER"));
	}

	private User createUser(String name, String email) {
		return createUser(name, email, Set.of(UserRole.USER));
	}

	private User createUser(String name, String email, Set<UserRole> roles) {
		return User.builder()
			.id(UUID.randomUUID())
			.name(name)
			.email(new Email(email))
			.password(new Password("password123"))
			.roles(roles)
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
