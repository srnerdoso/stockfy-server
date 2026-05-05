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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class FindUserByIdIT {

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String OTHER_ID = "00000000-0000-0000-0000-000000000003";

	private static final String MISSING_ID = "00000000-0000-0000-0000-000000009999";

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
	@DisplayName("Deve retornar detalhes quando usuario comum consultar o proprio ID")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void findUserById_whenOwnerRequestsOwnId_thenReturnsDetailedFields() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users/{id}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Bruno User"))
			.andExpect(jsonPath("$.email").value("bruno.user@example.com"))
			.andExpect(jsonPath("$.roles").isArray())
			.andExpect(jsonPath("$.roles[0]").value("USER"))
			.andExpect(jsonPath("$.role").doesNotExist())
			.andExpect(jsonPath("$.status").value("ACTIVE"))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.createdBy").value("11111111-1111-1111-1111-111111111111"))
			.andExpect(jsonPath("$.updatedAt").exists())
			.andExpect(jsonPath("$.updatedBy").value("22222222-2222-2222-2222-222222222222"))
			.andExpect(jsonPath("$.id").doesNotExist())
			.andExpect(jsonPath("$.active").doesNotExist())
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist())
			.andExpect(jsonPath("$.resetCode").doesNotExist())
			.andExpect(jsonPath("$.resetPasswordCodeHash").doesNotExist())
			.andExpect(jsonPath("$.resetPasswordExpiresAt").doesNotExist());

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar detalhes quando ADMIN consultar usuario comum")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void findUserById_whenAdminRequestsUserId_thenReturnsDetailedFields() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users/{id}", OTHER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Alice Filter"))
			.andExpect(jsonPath("$.email").value("alice.filter@example.com"))
			.andExpect(jsonPath("$.roles").isArray())
			.andExpect(jsonPath("$.roles[0]").value("USER"))
			.andExpect(jsonPath("$.role").doesNotExist())
			.andExpect(jsonPath("$.status").value("ACTIVE"))
			.andExpect(jsonPath("$.createdAt").exists())
			.andExpect(jsonPath("$.createdBy").exists())
			.andExpect(jsonPath("$.updatedAt").exists())
			.andExpect(jsonPath("$.updatedBy").exists())
			.andExpect(jsonPath("$.id").doesNotExist())
			.andExpect(jsonPath("$.active").doesNotExist())
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist())
			.andExpect(jsonPath("$.resetCode").doesNotExist())
			.andExpect(jsonPath("$.resetPasswordCodeHash").doesNotExist())
			.andExpect(jsonPath("$.resetPasswordExpiresAt").doesNotExist());

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("alice.filter@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando USER consultar ID de terceiro")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void findUserById_whenUserRequestsAnotherUserId_thenReturns403WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users/{id}", OTHER_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
		assertThat(userExists("alice.filter@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void findUserById_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users/{id}", OWNER_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void findUserById_whenIdIsInvalidUuid_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(get("/api/v1/users/{id}", "not-a-uuid")), "id",
				"O parâmetro informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void findUserById_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(get("/api/v1/users/{id}", "00000000-0000-0000-0000-000000000002' OR '1'='1")),
				"id", "O parâmetro informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
		assertThat(userExists("alice.filter@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 404 quando usuario nao existir")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void findUserById_whenUserDoesNotExist_thenReturns404() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users/{id}", MISSING_ID))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.detail").value("Usuário não encontrado."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void findUserById_whenLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i <= 11; i++) {
			var result = this.mockMvc.perform(get("/api/v1/users/{id}", OWNER_ID));
			if (i > 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve permitir nova consulta quando a janela de rate limit expirar")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void findUserById_whenRateLimitWindowExpires_thenReturns200() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < 10; i++) {
			this.mockMvc.perform(get("/api/v1/users/{id}", OWNER_ID)).andExpect(status().isOk());
		}

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc.perform(get("/api/v1/users/{id}", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("bruno.user@example.com"));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private boolean userExists(String email) {
		Integer count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class,
				email);
		return count != null && count == 1;
	}

}
