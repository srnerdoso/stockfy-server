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

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
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
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.AvoidCatchingGenericException",
		"PMD.AvoidDuplicateLiterals", "PMD.AvoidLiteralsInIfCondition" })
class UpdateCurrentUserIT {

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String OTHER_ID = "00000000-0000-0000-0000-000000000003";

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
		try {
			var loginBucketsField = UserRateLimitConfig.class.getDeclaredField("loginBuckets");
			loginBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) loginBucketsField.get(this.rateLimitConfig)).clear();

			var generalBucketsField = UserRateLimitConfig.class.getDeclaredField("generalBuckets");
			generalBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) generalBucketsField.get(this.rateLimitConfig)).clear();
			this.rateLimitTimeMeter.reset();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	@DisplayName("Deve retornar 204 e persistir nome e email quando usuario autenticado atualizar perfil")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenRequestIsValid_thenReturns204AndPersistsData() throws Exception {
		long usersBeforeRequest = countUsers();
		String json = """
				{
				  "name": "Bruno Updated",
				  "email": "bruno.updated@example.com"
				}
				""";

		this.mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userName(OWNER_ID)).isEqualTo("Bruno Updated");
		assertThat(userEmail(OWNER_ID)).isEqualTo("bruno.updated@example.com");
		assertUserHasRoles(OWNER_ID, "USER");
		assertThat(userStatus(OWNER_ID)).isEqualTo("ACTIVE");
		assertThat(userIsActive(OWNER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve atualizar somente nome quando email for omitido")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenEmailIsOmitted_thenKeepsCurrentEmail() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Only Name\"}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userName(OWNER_ID)).isEqualTo("Only Name");
		assertThat(userEmail(OWNER_ID)).isEqualTo("bruno.user@example.com");
	}

	@Test
	@DisplayName("Deve atualizar somente email quando nome for omitido")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenNameIsOmitted_thenKeepsCurrentName() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"only.email@example.com\"}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userName(OWNER_ID)).isEqualTo("Bruno User");
		assertThat(userEmail(OWNER_ID)).isEqualTo("only.email@example.com");
	}

	@Test
	@DisplayName("Nao deve aplicar campos protegidos da entidade enviados no corpo")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenRoleAndStatusAreSent_thenDoesNotAlterRoleOrStatus() throws Exception {
		long usersBeforeRequest = countUsers();
		String originalCreatedAt = userCreatedAt(OWNER_ID);
		String originalCreatedBy = userCreatedBy(OWNER_ID);
		setResetCodeData(OWNER_ID);
		String json = """
				{
				  "id": "99999999-9999-9999-9999-999999999999",
				  "name": "Bruno Safe",
				  "email": "bruno.safe@example.com",
				  "password": "new-password",
				  "passwordHash": "leaked-password-hash",
				  "roles": ["ADMIN"],
				  "status": "LOCKED",
				  "active": false,
				  "resetCode": "123456",
				  "resetPasswordCodeHash": "leaked-reset-hash",
				  "resetPasswordExpiresAt": "2099-12-31T23:59:59",
				  "createdAt": "2099-01-01T00:00:00",
				  "createdBy": "99999999-9999-9999-9999-999999999999",
				  "updatedAt": "1999-01-01T00:00:00",
				  "updatedBy": "99999999-9999-9999-9999-999999999999"
				}
				""";

		this.mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists(OWNER_ID)).isTrue();
		assertThat(userExists("99999999-9999-9999-9999-999999999999")).isFalse();
		assertThat(userName(OWNER_ID)).isEqualTo("Bruno Safe");
		assertThat(userEmail(OWNER_ID)).isEqualTo("bruno.safe@example.com");
		assertUserHasRoles(OWNER_ID, "USER");
		assertThat(userStatus(OWNER_ID)).isEqualTo("ACTIVE");
		assertThat(userIsActive(OWNER_ID)).isTrue();
		assertThat(passwordHash(OWNER_ID)).isEqualTo("password-hash-2");
		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo("existing-reset-hash");
		assertThat(resetPasswordExpiresAt(OWNER_ID).startsWith("2026-05-01")).isTrue();
		assertThat(userCreatedAt(OWNER_ID)).isEqualTo(originalCreatedAt);
		assertThat(userCreatedBy(OWNER_ID)).isEqualTo(originalCreatedBy);
		assertThat(userUpdatedAt(OWNER_ID).startsWith("1999-01-01")).isFalse();
		assertThat("99999999-9999-9999-9999-999999999999".equals(userUpdatedBy(OWNER_ID))).isFalse();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void updateCurrentUser_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc
			.perform(
					patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"No Auth\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userName(OWNER_ID)).isEqualTo("Bruno User");
	}

	@Test
	@DisplayName("Deve retornar 400 quando email for invalido")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenEmailIsInvalid_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"invalid-email\"}")),
				"email", "O e-mail informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userEmail(OWNER_ID)).isEqualTo("bruno.user@example.com");
	}

	@Test
	@DisplayName("Deve retornar 400 quando nome contiver SQL Injection")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenNameContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"User'); DROP TABLE users; --\"}")),
				"name", "O nome contém caracteres inválidos.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists(OWNER_ID)).isTrue();
		assertThat(userExists(OTHER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 409 quando email pertencer a outro usuario")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenEmailBelongsToAnotherUser_thenReturns409AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"alice.filter@example.com\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Conflict"))
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.detail").value("O e-mail informado já está em uso."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userEmail(OWNER_ID)).isEqualTo("bruno.user@example.com");
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i <= 10; i++) {
			var result = this.mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"invalid-email\"}"));
			if (i == 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userEmail(OWNER_ID)).isEqualTo("bruno.user@example.com");
	}

	@Test
	@DisplayName("Deve permitir nova atualizacao quando a janela de rate limit expirar")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < 10; i++) {
			this.mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"invalid-email\"}"));
		}

		this.mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"invalid-email\"}"))
			.andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"After Window\"}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userName(OWNER_ID)).isEqualTo("After Window");
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private boolean userExists(String id) {
		Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?::uuid", Integer.class,
				id);
		return count != null && count == 1;
	}

	private String userName(String id) {
		return this.jdbcTemplate.queryForObject("SELECT name FROM users WHERE id = ?::uuid", String.class, id);
	}

	private String userEmail(String id) {
		return this.jdbcTemplate.queryForObject("SELECT email FROM users WHERE id = ?::uuid", String.class, id);
	}

	private void assertUserHasRoles(String id, String... expectedRoles) {
		var roles = this.jdbcTemplate.queryForList("SELECT role FROM users_roles WHERE user_id = ?::uuid ORDER BY role",
				String.class, id);
		assertThat(roles.size()).isEqualTo(expectedRoles.length);
		for (String expectedRole : expectedRoles) {
			assertThat(roles.contains(expectedRole)).isTrue();
		}
	}

	private String userStatus(String id) {
		return this.jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, id);
	}

	private boolean userIsActive(String id) {
		Boolean active = this.jdbcTemplate.queryForObject("SELECT active FROM users WHERE id = ?::uuid", Boolean.class,
				id);
		return Boolean.TRUE.equals(active);
	}

	private String passwordHash(String id) {
		return this.jdbcTemplate.queryForObject("SELECT password_hash FROM users WHERE id = ?::uuid", String.class, id);
	}

	private void setResetCodeData(String id) {
		this.jdbcTemplate.update(
				"UPDATE users SET reset_password_code_hash = ?, reset_password_expires_at = ? " + "WHERE id = ?::uuid",
				"existing-reset-hash", java.sql.Timestamp.valueOf("2026-05-01 10:00:00"), id);
	}

	private String resetPasswordCodeHash(String id) {
		return this.jdbcTemplate.queryForObject("SELECT reset_password_code_hash FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private String resetPasswordExpiresAt(String id) {
		return this.jdbcTemplate.queryForObject("SELECT reset_password_expires_at::text FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private String userCreatedAt(String id) {
		return this.jdbcTemplate.queryForObject("SELECT created_at::text FROM users WHERE id = ?::uuid", String.class,
				id);
	}

	private String userCreatedBy(String id) {
		return this.jdbcTemplate.queryForObject("SELECT created_by::text FROM users WHERE id = ?::uuid", String.class,
				id);
	}

	private String userUpdatedAt(String id) {
		return this.jdbcTemplate.queryForObject("SELECT updated_at::text FROM users WHERE id = ?::uuid", String.class,
				id);
	}

	private String userUpdatedBy(String id) {
		return this.jdbcTemplate.queryForObject("SELECT updated_by::text FROM users WHERE id = ?::uuid", String.class,
				id);
	}

}
