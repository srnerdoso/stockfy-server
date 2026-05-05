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
import org.springframework.security.test.context.support.WithMockUser;
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
class FindAllUsersIT {

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
	@DisplayName("Deve retornar pagina SUMMARY apenas com nome, email e roles")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenTypeSummary_thenReturnsOnlySummaryFields() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY").param("page", "0").param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(4))
			.andExpect(jsonPath("$.content[0].name").exists())
			.andExpect(jsonPath("$.content[0].email").exists())
			.andExpect(jsonPath("$.content[0].roles").isArray())
			.andExpect(jsonPath("$.content[0].role").doesNotExist())
			.andExpect(jsonPath("$.content[0].id").doesNotExist())
			.andExpect(jsonPath("$.content[0].status").doesNotExist())
			.andExpect(jsonPath("$.content[0].active").doesNotExist())
			.andExpect(jsonPath("$.content[0].createdAt").doesNotExist())
			.andExpect(jsonPath("$.content[0].createdBy").doesNotExist())
			.andExpect(jsonPath("$.content[0].updatedAt").doesNotExist())
			.andExpect(jsonPath("$.content[0].updatedBy").doesNotExist())
			.andExpect(jsonPath("$.content[0].password").doesNotExist())
			.andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
			.andExpect(jsonPath("$.content[0].resetCode").doesNotExist())
			.andExpect(jsonPath("$.content[0].resetPasswordCodeHash").doesNotExist())
			.andExpect(jsonPath("$.content[0].resetPasswordExpiresAt").doesNotExist())
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(10))
			.andExpect(jsonPath("$.totalElements").value(4));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar pagina DETAILED com status e auditoria")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenTypeDetailed_thenReturnsDetailedFields() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users").param("type", "DETAILED").param("page", "0").param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name").exists())
			.andExpect(jsonPath("$.content[0].email").exists())
			.andExpect(jsonPath("$.content[0].roles").isArray())
			.andExpect(jsonPath("$.content[0].role").doesNotExist())
			.andExpect(jsonPath("$.content[0].status").exists())
			.andExpect(jsonPath("$.content[0].createdAt").exists())
			.andExpect(jsonPath("$.content[0].createdBy").exists())
			.andExpect(jsonPath("$.content[0].updatedAt").exists())
			.andExpect(jsonPath("$.content[0].updatedBy").exists())
			.andExpect(jsonPath("$.content[0].id").doesNotExist())
			.andExpect(jsonPath("$.content[0].active").doesNotExist())
			.andExpect(jsonPath("$.content[0].password").doesNotExist())
			.andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
			.andExpect(jsonPath("$.content[0].resetCode").doesNotExist())
			.andExpect(jsonPath("$.content[0].resetPasswordCodeHash").doesNotExist())
			.andExpect(jsonPath("$.content[0].resetPasswordExpiresAt").doesNotExist());

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve filtrar usuarios por nome parcial")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenNameFilterIsProvided_thenReturnsMatchingUsers() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY").param("name", "Ali"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].name").value("Alice Filter"));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("alice.filter@example.com")).isTrue();
		assertThat(userExists("bob.filter@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve respeitar os limites de paginacao")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenPageSizeIsOne_thenReturnsOneItemAndTotal() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY").param("page", "0").param("size", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.size").value(1))
			.andExpect(jsonPath("$.totalElements").value(4))
			.andExpect(jsonPath("$.totalPages").value(4));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando type for omitido")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenTypeIsMissing_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(this.mockMvc.perform(get("/api/v1/users")), "type",
				"O parâmetro informado é obrigatório.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando type for invalido")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenTypeIsInvalid_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(get("/api/v1/users").param("type", "FULL")), "type",
				"O parâmetro informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando name exceder o limite da coluna")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenNameExceedsColumnLimit_thenReturns400() throws Exception {
		String longName = "a".repeat(256);
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY").param("name", longName)), "name",
				"O nome deve ter no maximo 255 caracteres.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("alice.filter@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 401 quando nao autenticado")
	void findAllUsers_whenNotAuthenticated_thenReturns401() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 403 quando usuario nao for ADMIN")
	@WithMockUser(roles = "USER")
	void findAllUsers_whenUserIsNotAdmin_thenReturns403() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY"))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("bruno.user@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando name contiver SQL Injection")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenNameContainsSqlInjection_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(this.mockMvc
			.perform(get("/api/v1/users").param("type", "SUMMARY").param("name", "x%' OR 1=1; DROP TABLE users; --")),
				"name", "O nome contém caracteres inválidos.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
		assertThat(userExists("alice.filter@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i <= 11; i++) {
			var result = this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY"));
			if (i > 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
	}

	@Test
	@DisplayName("Deve permitir nova requisicao quando a janela de rate limit expirar")
	@WithMockUser(roles = "ADMIN")
	void findAllUsers_whenRateLimitWindowExpires_thenReturns200() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < 10; i++) {
			this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY")).andExpect(status().isOk());
		}

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(4));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists("ana.admin@example.com")).isTrue();
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
