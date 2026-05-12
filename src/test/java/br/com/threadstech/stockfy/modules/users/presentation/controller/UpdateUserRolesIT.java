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

package br.com.threadstech.stockfy.modules.users.presentation.controller;

import java.time.Duration;
import java.util.List;

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitBucketCleaner;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UpdateUserRolesIT {

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String USER_ID = "00000000-0000-0000-0000-000000000002";

	private static final int GENERAL_RATE_LIMIT = 10;

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String USER_ROLES_ENDPOINT = "/api/v1/users/{id}/roles";

	private static final String ADD_ADMIN_ROLE_BODY = "{\"add\":[\"ADMIN\"]}";

	private static final String USER_ROLE = "USER";

	private static final String ADD_FIRST_FIELD = "add[0]";

	private static final String INVALID_ROLE_MESSAGE = "O perfil informado é inválido.";

	private static final String INVALID_UUID = "not-a-uuid";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDownRateLimit() {
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
	}

	@Test
	@DisplayName("Deve retornar 204 e persistir ADMIN sem alterar outros dados do usuario")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenAdminAddsAdminRole_thenReturns204AndPersistsRoles() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot oldUser = userSnapshot(USER_ID);

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"add\":[\"ADMIN\"],\"remove\":[]}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertUserHasRoles(USER_ID, ADMIN_ROLE, USER_ROLE);
		assertUnchangedUserData(USER_ID, oldUser);
	}

	@Test
	@DisplayName("Deve retornar 204 e persistir remocao de role")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenAdminRemovesUserRole_thenReturns204AndPersistsRoles() throws Exception {
		this.jdbcTemplate.update("INSERT INTO users_roles (user_id, role) VALUES (?::uuid, ?)", USER_ID, ADMIN_ROLE);
		UserSnapshot oldUser = userSnapshot(USER_ID);

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"remove\":[\"USER\"]}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertUserHasRoles(USER_ID, ADMIN_ROLE);
		assertUnchangedUserData(USER_ID, oldUser);
	}

	@Test
	@DisplayName("Nao deve aplicar campos de usuario enviados junto com roles")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenProtectedFieldsAreSent_thenChangesOnlyRoles() throws Exception {
		UserSnapshot oldUser = userSnapshot(USER_ID);

		this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
			.content("{\"add\":[\"ADMIN\"],\"name\":\"Injected\",\"email\":\"injected@example.com\",\"active\":false}"))
			.andExpect(status().isNoContent());

		assertUserHasRoles(USER_ID, ADMIN_ROLE, USER_ROLE);
		assertUnchangedUserData(USER_ID, oldUser);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void updateUserRoles_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(ADD_ADMIN_ROLE_BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando usuario autenticado nao for ADMIN")
	@WithMockUserId(id = USER_ID, roles = "USER")
	void updateUserRoles_whenAuthenticatedUserIsNotAdmin_thenReturns403WithoutBody() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(ADD_ADMIN_ROLE_BODY))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, INVALID_UUID).contentType(MediaType.APPLICATION_JSON)
					.content(ADD_ADMIN_ROLE_BODY)),
				"id", "O parâmetro informado é inválido.");

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, "00000000-0000-0000-0000-000000000002' OR '1'='1")
					.contentType(MediaType.APPLICATION_JSON)
					.content(ADD_ADMIN_ROLE_BODY)),
				"id", "O parâmetro informado é inválido.");

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 400 quando role for invalida")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenRoleIsInvalid_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
					.content("{\"add\":[\"INVALID\"]}")),
				ADD_FIRST_FIELD, INVALID_ROLE_MESSAGE);

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 400 quando role for nula")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenRoleIsNull_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
					.content("{\"add\":[null]}")),
				ADD_FIRST_FIELD, INVALID_ROLE_MESSAGE);

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 400 quando role estiver ausente no payload")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenRoleIsBlank_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
					.content("{\"add\":[\"\"]}")),
				ADD_FIRST_FIELD, INVALID_ROLE_MESSAGE);

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 400 quando role contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenRoleContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		ApiErrorResponseAssertions
			.assertBadRequestFieldValidation(
					this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
						.content("{\"add\":[\"ADMIN'); DROP TABLE users; --\"]}")),
					ADD_FIRST_FIELD, INVALID_ROLE_MESSAGE);

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
		assertThat(countUsers()).isEqualTo(4);
	}

	@Test
	@DisplayName("Deve retornar 400 quando operacao deixar usuario sem role")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenOperationLeavesUserWithoutRoles_thenReturns400AndDoesNotAlterData() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
				.content("{\"remove\":[\"USER\"]}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.detail").value("A operação de perfis deixaria o usuário sem perfil válido."))
			.andExpect(jsonPath("$.instance").value("/api/v1/users/" + USER_ID + "/roles"))
			.andExpect(jsonPath("$.fieldErrors.length()").value(1))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("roles"))
			.andExpect(jsonPath("$.fieldErrors[0].message")
				.value("A operação de perfis deixaria o usuário sem perfil válido."));

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenLimitExceeded_thenReturns429() throws Exception {
		List<String> oldRoles = userRoles(USER_ID);

		for (int i = 0; i <= GENERAL_RATE_LIMIT; i++) {
			var result = this.mockMvc
				.perform(patch(USER_ROLES_ENDPOINT, INVALID_UUID).contentType(MediaType.APPLICATION_JSON)
					.content(ADD_ADMIN_ROLE_BODY));
			if (i == GENERAL_RATE_LIMIT) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(userRoles(USER_ID)).isEqualTo(oldRoles);
	}

	@Test
	@DisplayName("Deve processar requisicao quando janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void updateUserRoles_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			this.mockMvc.perform(patch(USER_ROLES_ENDPOINT, INVALID_UUID).contentType(MediaType.APPLICATION_JSON)
				.content(ADD_ADMIN_ROLE_BODY));
		}

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, INVALID_UUID).contentType(MediaType.APPLICATION_JSON)
				.content(ADD_ADMIN_ROLE_BODY))
			.andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc
			.perform(patch(USER_ROLES_ENDPOINT, USER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(ADD_ADMIN_ROLE_BODY))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertUserHasRoles(USER_ID, ADMIN_ROLE, USER_ROLE);
	}

	private void assertUserHasRoles(String userId, String... expectedRoles) {
		List<String> roles = userRoles(userId);
		assertThat(roles.size()).isEqualTo(expectedRoles.length);
		for (String expected : expectedRoles) {
			assertThat(roles.contains(expected)).isTrue();
		}
	}

	private List<String> userRoles(String userId) {
		return this.jdbcTemplate.queryForList("SELECT role FROM users_roles WHERE user_id = ?::uuid ORDER BY role",
				String.class, userId);
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private UserSnapshot userSnapshot(String userId) {
		return this.jdbcTemplate.queryForObject("""
				SELECT password_hash, status, active, reset_password_code_hash,
				       reset_password_expires_at::text, created_at::text, created_by::text,
				       updated_at::text, updated_by::text
				  FROM users
				 WHERE id = ?::uuid
				""",
				(rs, rowNum) -> new UserSnapshot(rs.getString("password_hash"), rs.getString("status"),
						rs.getBoolean("active"), rs.getString("reset_password_code_hash"),
						rs.getString("reset_password_expires_at"), rs.getString("created_at"),
						rs.getString("created_by"), rs.getString("updated_at"), rs.getString("updated_by")),
				userId);
	}

	private void assertUnchangedUserData(String userId, UserSnapshot oldUser) {
		UserSnapshot currentUser = userSnapshot(userId);
		assertThat(currentUser.passwordHash()).isEqualTo(oldUser.passwordHash());
		assertThat(currentUser.status()).isEqualTo(oldUser.status());
		assertThat(currentUser.active()).isEqualTo(oldUser.active());
		assertThat(currentUser.resetPasswordCodeHash()).isEqualTo(oldUser.resetPasswordCodeHash());
		assertThat(currentUser.resetPasswordExpiresAt()).isEqualTo(oldUser.resetPasswordExpiresAt());
		assertThat(currentUser.createdAt()).isEqualTo(oldUser.createdAt());
		assertThat(currentUser.createdBy()).isEqualTo(oldUser.createdBy());
	}

	private record UserSnapshot(String passwordHash, String status, boolean active, String resetPasswordCodeHash,
			String resetPasswordExpiresAt, String createdAt, String createdBy, String updatedAt, String updatedBy) {
	}

}
