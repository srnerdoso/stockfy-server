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

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.users.application.usecase.LoginUseCase;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
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

import static br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions.assertBadRequestFieldValidation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DeleteUserByIdIT {

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String OTHER_ID = "00000000-0000-0000-0000-000000000003";

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
		try {
			var loginBucketsField = UserRateLimitConfig.class.getDeclaredField("loginBuckets");
			loginBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) loginBucketsField.get(rateLimitConfig)).clear();

			var generalBucketsField = UserRateLimitConfig.class.getDeclaredField("generalBuckets");
			generalBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) generalBucketsField.get(rateLimitConfig)).clear();
			rateLimitTimeMeter.reset();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Deve retornar 204 e aplicar soft delete quando ADMIN deletar ID existente")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void deleteUserById_whenAdminDeletesExistingUser_thenReturns204AndSoftDeletesUser() throws Exception {
		long usersBeforeRequest = countUsers();
		assertTrue(userIsActive(OWNER_ID));

		mockMvc.perform(delete("/api/v1/users/{id}", OWNER_ID))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userExists(OWNER_ID));
		assertFalse(userIsActive(OWNER_ID));
	}

	@Test
	@DisplayName("Deve impedir login apos usuario ser deletado")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void deleteUserById_whenUserWasDeleted_thenUserCannotLogin() throws Exception {
		jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?", passwordEncoder.encode("password123"),
				UUID.fromString(OWNER_ID));

		mockMvc.perform(delete("/api/v1/users/{id}", OWNER_ID)).andExpect(status().isNoContent());

		assertThrows(InvalidCredentialsException.class,
				() -> loginUseCase.execute("bruno.user@example.com", "password123"));

		assertTrue(userExists(OWNER_ID));
		assertFalse(userIsActive(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando USER tentar deletar a propria conta")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void deleteUserById_whenOwnerUserDeletesOwnAccount_thenReturns403WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		mockMvc.perform(delete("/api/v1/users/{id}", OWNER_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userIsActive(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void deleteUserById_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		mockMvc.perform(delete("/api/v1/users/{id}", OWNER_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userIsActive(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void deleteUserById_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(mockMvc.perform(delete("/api/v1/users/{id}", "not-a-uuid")), "id",
				"O parâmetro informado é inválido.");

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userIsActive(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void deleteUserById_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(delete("/api/v1/users/{id}", "00000000-0000-0000-0000-000000000002' OR '1'='1")), "id",
				"O parâmetro informado é inválido.");

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userIsActive(OWNER_ID));
		assertTrue(userIsActive(OTHER_ID));
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void deleteUserById_whenLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i <= 10; i++) {
			var result = mockMvc.perform(delete("/api/v1/users/{id}", "not-a-uuid"));
			if (i == 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userIsActive(OWNER_ID));
		assertTrue(userIsActive(OTHER_ID));
	}

	@Test
	@DisplayName("Deve permitir nova tentativa quando a janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void deleteUserById_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < 10; i++) {
			mockMvc.perform(delete("/api/v1/users/{id}", "not-a-uuid"));
		}

		mockMvc.perform(delete("/api/v1/users/{id}", "not-a-uuid")).andExpect(status().isTooManyRequests());

		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		mockMvc.perform(delete("/api/v1/users/{id}", OTHER_ID)).andExpect(status().isNoContent());

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userIsActive(OWNER_ID));
		assertFalse(userIsActive(OTHER_ID));
	}

	private long countUsers() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return count == null ? 0 : count;
	}

	private boolean userExists(String id) {
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?::uuid", Integer.class, id);
		return count != null && count == 1;
	}

	private boolean userIsActive(String id) {
		Boolean active = jdbcTemplate.queryForObject("SELECT active FROM users WHERE id = ?::uuid", Boolean.class, id);
		return Boolean.TRUE.equals(active);
	}

}
