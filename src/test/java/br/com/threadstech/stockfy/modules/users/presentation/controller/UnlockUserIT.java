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

import static br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions.assertBadRequestFieldValidation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UnlockUserIT {

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String LOCKED_ID = "00000000-0000-0000-0000-000000000004";

	private static final String LOCKED_EMAIL = "bob.filter@example.com";

	private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";

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
		clearRateLimitBuckets();
		redisTemplate.delete(LOGIN_ATTEMPTS_KEY + LOCKED_EMAIL);
		rateLimitTimeMeter.reset();
	}

	@Test
	@DisplayName("Deve retornar 204, desbloquear usuario e limpar tentativas de login")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void unlockUser_whenAdminUnlocksLockedUser_thenReturns204PersistsStatusAndClearsAttempts() throws Exception {
		long usersBeforeRequest = countUsers();
		lockUser(LOCKED_ID);
		redisTemplate.opsForValue().set(LOGIN_ATTEMPTS_KEY + LOCKED_EMAIL, "15");

		mockMvc.perform(patch("/api/v1/users/{id}/unlock", LOCKED_ID))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus(LOCKED_ID));
		assertNull(redisTemplate.opsForValue().get(LOGIN_ATTEMPTS_KEY + LOCKED_EMAIL));
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void unlockUser_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		mockMvc.perform(patch("/api/v1/users/{id}/unlock", LOCKED_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertEquals(oldStatus, userStatus(LOCKED_ID));
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando usuario autenticado nao for ADMIN")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void unlockUser_whenAuthenticatedUserIsNotAdmin_thenReturns403WithoutBody() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		mockMvc.perform(patch("/api/v1/users/{id}/unlock", LOCKED_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertEquals(oldStatus, userStatus(LOCKED_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void unlockUser_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		assertBadRequestFieldValidation(mockMvc.perform(patch("/api/v1/users/{id}/unlock", "not-a-uuid")), "id",
				"O parâmetro informado é inválido.");

		assertEquals(oldStatus, userStatus(LOCKED_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void unlockUser_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		assertBadRequestFieldValidation(
				mockMvc.perform(patch("/api/v1/users/{id}/unlock", "00000000-0000-0000-0000-000000000004' OR '1'='1")),
				"id", "O parâmetro informado é inválido.");

		assertEquals(oldStatus, userStatus(LOCKED_ID));
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void unlockUser_whenLimitExceeded_thenReturns429() throws Exception {
		String oldStatus = userStatus(LOCKED_ID);

		for (int i = 0; i <= 10; i++) {
			var result = mockMvc.perform(patch("/api/v1/users/{id}/unlock", "not-a-uuid"));
			if (i == 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertEquals(oldStatus, userStatus(LOCKED_ID));
	}

	@Test
	@DisplayName("Deve permitir desbloqueio quando a janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void unlockUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		lockUser(LOCKED_ID);

		for (int i = 0; i < 10; i++) {
			mockMvc.perform(patch("/api/v1/users/{id}/unlock", "not-a-uuid"));
		}

		mockMvc.perform(patch("/api/v1/users/{id}/unlock", "not-a-uuid")).andExpect(status().isTooManyRequests());

		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		mockMvc.perform(patch("/api/v1/users/{id}/unlock", LOCKED_ID))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals("ACTIVE", userStatus(LOCKED_ID));
	}

	private long countUsers() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return count == null ? 0 : count;
	}

	private void lockUser(String id) {
		jdbcTemplate.update("UPDATE users SET status = 'LOCKED' WHERE id = ?::uuid", id);
	}

	private String userStatus(String id) {
		return jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, id);
	}

	private void clearRateLimitBuckets() {
		try {
			clearBucketMap("loginBuckets");
			clearBucketMap("generalBuckets");
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private void clearBucketMap(String fieldName) throws ReflectiveOperationException {
		var field = UserRateLimitConfig.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		((java.util.Map<?, ?>) field.get(rateLimitConfig)).clear();
	}

}
