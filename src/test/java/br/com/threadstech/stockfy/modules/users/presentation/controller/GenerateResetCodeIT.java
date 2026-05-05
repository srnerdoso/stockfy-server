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
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.AvoidDuplicateLiterals",
		"PMD.AvoidLiteralsInIfCondition" })
class GenerateResetCodeIT {

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResetCodeHasher resetCodeHasher;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDownRateLimit() {
		clearRateLimitBuckets();
		this.rateLimitTimeMeter.reset();
	}

	@Test
	@DisplayName("Deve retornar 200, codigo e persistir hash e expiracao quando ADMIN solicitar")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenAdminRequestsExistingUser_thenReturns200AndPersistsHash() throws Exception {
		long usersBeforeRequest = countUsers();

		String response = this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isString())
			.andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("\\d{6}")))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist())
			.andExpect(jsonPath("$.resetPasswordCodeHash").doesNotExist())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String code = response.replaceAll(".*\"code\":\"(\\d{6})\".*", "$1");
		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo(this.resetCodeHasher.hash(code));
		assertThat(code.equals(resetPasswordCodeHash(OWNER_ID))).isFalse();
		assertThat(resetPasswordExpiresAt(OWNER_ID)).isNotNull();
	}

	@Test
	@DisplayName("Deve sobrescrever codigo anterior quando novo codigo for gerado")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenExistingCodeExists_thenReplacesStoredHashAndExpiration() throws Exception {
		setResetPasswordData(OWNER_ID, "old-hash");

		this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isString());

		assertThat("old-hash".equals(resetPasswordCodeHash(OWNER_ID))).isFalse();
		assertThat(resetPasswordExpiresAt(OWNER_ID)).isNotNull();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void generateResetCode_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando usuario autenticado nao for ADMIN")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void generateResetCode_whenAuthenticatedUserIsNotAdmin_thenReturns403WithoutBody() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid")), "id",
				"O parâmetro informado é inválido.");

		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(this.mockMvc.perform(
				post("/api/v1/users/{id}/password-reset-codes", "00000000-0000-0000-0000-000000000002' OR '1'='1")),
				"id", "O parâmetro informado é inválido.");

		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenLimitExceeded_thenReturns429() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		for (int i = 0; i <= 10; i++) {
			var result = this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid"));
			if (i == 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertThat(resetPasswordCodeHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve permitir nova geracao quando a janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		for (int i = 0; i < 10; i++) {
			this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid"));
		}

		this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid"))
			.andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isString());

		assertThat(resetPasswordCodeHash(OWNER_ID)).isNotNull();
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private void setResetPasswordData(String id, String hash) {
		this.jdbcTemplate.update("UPDATE users SET reset_password_code_hash = ?, reset_password_expires_at = now() "
				+ "WHERE id = ?::uuid", hash, id);
	}

	private String resetPasswordCodeHash(String id) {
		return this.jdbcTemplate.queryForObject("SELECT reset_password_code_hash FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private String resetPasswordExpiresAt(String id) {
		return this.jdbcTemplate.queryForObject("SELECT reset_password_expires_at::text FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private void clearRateLimitBuckets() {
		try {
			clearBucketMap("loginBuckets");
			clearBucketMap("generalBuckets");
			clearBucketMap("passwordBuckets");
		}
		catch (ReflectiveOperationException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void clearBucketMap(String fieldName) throws ReflectiveOperationException {
		var field = UserRateLimitConfig.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		((java.util.Map<?, ?>) field.get(this.rateLimitConfig)).clear();
	}

}
