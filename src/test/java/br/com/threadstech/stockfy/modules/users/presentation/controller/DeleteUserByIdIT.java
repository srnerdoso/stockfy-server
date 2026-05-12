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
import java.util.UUID;

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitBucketCleaner;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.users.application.usecase.LoginUseCase;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/endpoint-scenarios.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DeleteUserByIdIT {

	private static final int GENERAL_RATE_LIMIT = 10;

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000010";

	private static final String OTHER_ID = "00000000-0000-0000-0000-000000000011";

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String USER_BY_ID_ENDPOINT = "/api/v1/users/{id}";

	private static final String INVALID_UUID = "not-a-uuid";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private LoginUseCase loginUseCase;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDownRateLimit() {
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
	}

	@Test
	@DisplayName("Deve retornar 204 e aplicar soft delete quando ADMIN deletar ID existente")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void deleteUserById_whenAdminDeletesExistingUser_thenReturns204AndSoftDeletesUser() throws Exception {
		long usersBeforeRequest = countUsers();
		assertThat(userIsActive(OWNER_ID)).isTrue();
		this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, OWNER_ID))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userExists(OWNER_ID)).isTrue();
		assertThat(userIsActive(OWNER_ID)).isFalse();
		assertThat(userEmail(OWNER_ID)).contains(OWNER_ID).isNotEqualTo("delete.scenario@example.com");
	}

	@Test
	@DisplayName("Deve impedir login apos usuario ser deletado")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void deleteUserById_whenUserWasDeleted_thenUserCannotLogin() throws Exception {
		this.jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?",
				this.passwordEncoder.encode("password123"), UUID.fromString(OWNER_ID));

		this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, OWNER_ID)).andExpect(status().isNoContent());

		assertThatExceptionOfType(InvalidCredentialsException.class)
			.isThrownBy(() -> this.loginUseCase.execute("delete.scenario@example.com", "password123"));

		assertThat(userExists(OWNER_ID)).isTrue();
		assertThat(userIsActive(OWNER_ID)).isFalse();
		assertThat(userEmail(OWNER_ID)).contains(OWNER_ID).isNotEqualTo("delete.scenario@example.com");
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando USER tentar deletar a propria conta")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void deleteUserById_whenOwnerUserDeletesOwnAccount_thenReturns403WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, OWNER_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userIsActive(OWNER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void deleteUserById_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, OWNER_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userIsActive(OWNER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void deleteUserById_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, INVALID_UUID)), "id",
				"O parâmetro informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userIsActive(OWNER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void deleteUserById_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, "00000000-0000-0000-0000-000000000002' OR '1'='1")),
				"id", "O parâmetro informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userIsActive(OWNER_ID)).isTrue();
		assertThat(userIsActive(OTHER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void deleteUserById_whenLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i <= GENERAL_RATE_LIMIT; i++) {
			var result = this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, INVALID_UUID));
			if (i == GENERAL_RATE_LIMIT) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userIsActive(OWNER_ID)).isTrue();
		assertThat(userIsActive(OTHER_ID)).isTrue();
	}

	@Test
	@DisplayName("Deve permitir nova tentativa quando a janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void deleteUserById_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, INVALID_UUID));
		}

		this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, INVALID_UUID)).andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc.perform(delete(USER_BY_ID_ENDPOINT, OTHER_ID)).andExpect(status().isNoContent());

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userIsActive(OWNER_ID)).isTrue();
		assertThat(userIsActive(OTHER_ID)).isFalse();
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

	private boolean userIsActive(String id) {
		Boolean active = this.jdbcTemplate.queryForObject("SELECT active FROM users WHERE id = ?::uuid", Boolean.class,
				id);
		return active != null && active;
	}

	private String userEmail(String id) {
		return this.jdbcTemplate.queryForObject("SELECT email FROM users WHERE id = ?::uuid", String.class, id);
	}

}
