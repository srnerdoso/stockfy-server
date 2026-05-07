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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UnlockUserIT {

	private static final int GENERAL_RATE_LIMIT = 10;

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String LOCKED_ID = "00000000-0000-0000-0000-000000000004";

	private static final String LOCKED_EMAIL = "bob.filter@example.com";

	private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String UNLOCK_USER_ENDPOINT = "/api/v1/users/{id}/unlock";

	private static final String INVALID_UUID = "not-a-uuid";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDown() {
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
		this.redisTemplate.delete(LOGIN_ATTEMPTS_KEY + LOCKED_EMAIL);
	}

	@Test
	@DisplayName("Deve retornar 204, desbloquear usuario e limpar tentativas de login")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void unlockUser_whenAdminUnlocksLockedUser_thenReturns204PersistsStatusAndClearsAttempts() throws Exception {
		long usersBeforeRequest = countUsers();
		lockUser(LOCKED_ID);
		this.redisTemplate.opsForValue().set(LOGIN_ATTEMPTS_KEY + LOCKED_EMAIL, "15");

		this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, LOCKED_ID))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus(LOCKED_ID)).isEqualTo("ACTIVE");
		assertThat(this.redisTemplate.opsForValue().get(LOGIN_ATTEMPTS_KEY + LOCKED_EMAIL)).isNull();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void unlockUser_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, LOCKED_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(userStatus(LOCKED_ID)).isEqualTo(oldStatus);
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando usuario autenticado nao for ADMIN")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void unlockUser_whenAuthenticatedUserIsNotAdmin_thenReturns403WithoutBody() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, LOCKED_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertThat(userStatus(LOCKED_ID)).isEqualTo(oldStatus);
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void unlockUser_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, INVALID_UUID)), "id",
				"O parâmetro informado é inválido.");

		assertThat(userStatus(LOCKED_ID)).isEqualTo(oldStatus);
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void unlockUser_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, "00000000-0000-0000-0000-000000000004' OR '1'='1")),
				"id", "O parâmetro informado é inválido.");

		assertThat(userStatus(LOCKED_ID)).isEqualTo(oldStatus);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void unlockUser_whenLimitExceeded_thenReturns429() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		for (int i = 0; i <= GENERAL_RATE_LIMIT; i++) {
			var result = this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, INVALID_UUID));
			if (i == GENERAL_RATE_LIMIT) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(userStatus(LOCKED_ID)).isEqualTo(oldStatus);
	}

	@Test
	@DisplayName("Deve permitir desbloqueio quando a janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = ADMIN_ROLE)
	void unlockUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		lockUser(LOCKED_ID);

		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, INVALID_UUID));
		}

		this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, INVALID_UUID)).andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc.perform(patch(UNLOCK_USER_ENDPOINT, LOCKED_ID))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(userStatus(LOCKED_ID)).isEqualTo("ACTIVE");
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private void lockUser(String id) {
		this.jdbcTemplate.update("UPDATE users SET status = 'LOCKED' WHERE id = ?::uuid", id);
	}

	private String userStatus(String id) {
		return this.jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, id);
	}

}
