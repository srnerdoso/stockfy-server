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

import br.com.threadstech.stockfy.ContainersConfiguration;
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
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ContainersConfiguration.class)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserAuditIT {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("Deve registrar trilha create update delete na tabela users_aud")
	void userMutations_whenPerformed_thenCreatesAuditHistoryInUsersAud() {
		User user = createUser("Audit User", "audit-user@example.com", Set.of(UserRole.USER));
		this.userRepository.save(user);

		user.setName("Audit User Updated");
		this.userRepository.update(user);
		this.userRepository.deleteById(user.getId());

		Integer revisions = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users_aud WHERE id = ?::uuid",
				Integer.class, user.getId());
		Integer createRevisions = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users_aud WHERE id = ?::uuid AND revtype = 0", Integer.class, user.getId());
		Integer updateRevisions = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users_aud WHERE id = ?::uuid AND revtype = 1", Integer.class, user.getId());
		Integer deleteRevisions = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users_aud WHERE id = ?::uuid AND revtype = 2", Integer.class, user.getId());
		assertThat(revisions).isEqualTo(3);
		assertThat(createRevisions).isEqualTo(1);
		assertThat(updateRevisions).isEqualTo(1);
		assertThat(deleteRevisions).isEqualTo(1);
	}

	@Test
	@DisplayName("Deve registrar auditoria de roles na tabela users_roles_aud")
	void userRoleMutation_whenPerformed_thenCreatesAuditHistoryInUsersRolesAud() {
		User user = createUser("Role Audit", "role-audit@example.com", Set.of(UserRole.USER));
		this.userRepository.save(user);
		user.updateRoles(Set.of(UserRole.ADMIN), Set.of());
		this.userRepository.update(user);

		Integer rolesAudRows = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users_roles_aud WHERE user_id = ?::uuid", Integer.class, user.getId());
		Integer adminRoleAudRows = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users_roles_aud WHERE user_id = ?::uuid AND role = 'ADMIN'", Integer.class,
				user.getId());
		assertThat(rolesAudRows).isGreaterThanOrEqualTo(1);
		assertThat(adminRoleAudRows).isEqualTo(1);
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
