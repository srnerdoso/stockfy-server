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

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.AvoidDuplicateLiterals",
		"PMD.AvoidLiteralsInIfCondition" })
class UpdatePasswordIT {

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

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
	@DisplayName("Deve retornar 204, atualizar senha e revogar codigo quando code for valido")
	void updatePassword_whenResetCodeIsValid_thenReturns204UpdatesPasswordAndRevokesCode() throws Exception {
		configureResetCode(OWNER_ID, "123456", LocalDateTime.now().plusHours(1));
		String oldHash = passwordHash(OWNER_ID);

		this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "code": "123456",
				  "newPassword": "newPassword123",
				  "confirmPassword": "newPassword123"
				}
				""")).andExpect(status().isNoContent()).andExpect(content().string(""));

		String newHash = passwordHash(OWNER_ID);
		assertThat(newHash).isNotEqualTo(oldHash);
		assertThat(this.passwordEncoder.matches("newPassword123", newHash)).isTrue();
		assertThat(resetPasswordCodeExists(OWNER_ID)).isFalse();
		assertThat(resetPasswordExpiresAtExists(OWNER_ID)).isFalse();
	}

	@Test
	@DisplayName("Deve retornar 422 quando tentar reutilizar o mesmo code")
	void updatePassword_whenResetCodeIsReused_thenReturns422AndDoesNotAlterPassword() throws Exception {
		configureResetCode(OWNER_ID, "123456", LocalDateTime.now().plusHours(1));

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"123456\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isNoContent());
		String hashAfterFirstUse = passwordHash(OWNER_ID);

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"123456\",\"newPassword\":\"anotherPassword123\","
						+ "\"confirmPassword\":\"anotherPassword123\"}"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("Código de recuperação inválido ou expirado."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(hashAfterFirstUse);
	}

	@Test
	@DisplayName("Deve retornar 422 quando code estiver expirado")
	void updatePassword_whenResetCodeIsExpired_thenReturns422AndDoesNotAlterPassword() throws Exception {
		configureResetCode(OWNER_ID, "123456", LocalDateTime.now().minusMinutes(1));
		String oldHash = passwordHash(OWNER_ID);

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"123456\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("Código de recuperação inválido ou expirado."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 204 quando usuario autenticado informar senha atual correta")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updatePassword_whenAuthenticatedAndCurrentPasswordIsCorrect_thenReturns204() throws Exception {
		configurePassword(OWNER_ID, "oldPassword123");

		this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "currentPassword": "oldPassword123",
				  "newPassword": "newPassword123",
				  "confirmPassword": "newPassword123"
				}
				""")).andExpect(status().isNoContent()).andExpect(content().string(""));

		assertThat(this.passwordEncoder.matches("newPassword123", passwordHash(OWNER_ID))).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando code nao for enviado e usuario nao autenticado")
	void updatePassword_whenCodeIsMissingAndUserIsNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"newPassword\":\"newPassword123\"," + "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 400 quando newPassword for omitido")
	void updatePassword_whenNewPasswordIsMissing_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
					.content("{\"code\":\"123456\",\"confirmPassword\":\"newPassword123\"}")),
				"newPassword", "A senha é obrigatória.");

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 400 quando code contiver SQL Injection")
	void updatePassword_whenCodeContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
					.content("{\"code\":\"123456' OR '1'='1\"," + "\"newPassword\":\"newPassword123\","
							+ "\"confirmPassword\":\"newPassword123\"}")),
				"code", "O código de recuperação é inválido.");

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 422 quando senhas nao coincidirem")
	void updatePassword_whenPasswordsDoNotMatch_thenReturns422AndDoesNotAlterData() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"123456\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"differentPassword123\"}"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("As senhas não coincidem."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors.length()").value(1))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("As senhas não coincidem."));

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 422 quando senha atual for incorreta")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updatePassword_whenCurrentPasswordIsWrong_thenReturns422AndDoesNotAlterData() throws Exception {
		configurePassword(OWNER_ID, "oldPassword123");
		String oldHash = passwordHash(OWNER_ID);

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"currentPassword\":\"wrongPassword123\"," + "\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("Senha atual incorreta."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 422 quando senha atual for omitida no fluxo autenticado")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updatePassword_whenCurrentPasswordIsMissing_thenReturns422AndDoesNotAlterData() throws Exception {
		configurePassword(OWNER_ID, "oldPassword123");
		String oldHash = passwordHash(OWNER_ID);

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"newPassword\":\"newPassword123\"," + "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("Senha atual incorreta."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve bloquear usuario autenticado apos 15 tentativas invalidas")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updatePassword_whenCurrentPasswordFailsFifteenTimes_thenLocksUser() throws Exception {
		configurePassword(OWNER_ID, "oldPassword123");

		for (int i = 0; i < 15; i++) {
			this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"currentPassword\":\"wrongPassword123\"," + "\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"));
			if ((i + 1) % 5 == 0) {
				this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));
			}
		}

		assertThat(userStatus(OWNER_ID)).isEqualTo("LOCKED");
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite de 5 requisicoes por minuto")
	void updatePassword_whenLimitExceeded_thenReturns429() throws Exception {
		for (int i = 0; i <= 5; i++) {
			var result = this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"999999\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"));
			if (i == 5) {
				result.andExpect(status().isTooManyRequests());
			}
		}
	}

	@Test
	@DisplayName("Deve permitir nova tentativa quando a janela de rate limit expirar")
	void updatePassword_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		for (int i = 0; i < 5; i++) {
			this.mockMvc.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"999999\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"));
		}

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"999999\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc
			.perform(patch("/api/v1/users/password").contentType(MediaType.APPLICATION_JSON)
				.content("{\"code\":\"999999\",\"newPassword\":\"newPassword123\","
						+ "\"confirmPassword\":\"newPassword123\"}"))
			.andExpect(status().isUnprocessableEntity());
	}

	private void configureResetCode(String id, String rawCode, LocalDateTime expiresAt) {
		this.jdbcTemplate.update(
				"UPDATE users SET reset_password_code_hash = ?, reset_password_expires_at = ? " + "WHERE id = ?::uuid",
				this.resetCodeHasher.hash(rawCode), Timestamp.valueOf(expiresAt), id);
	}

	private void configurePassword(String id, String rawPassword) {
		this.jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?::uuid",
				this.passwordEncoder.encode(rawPassword), id);
	}

	private String passwordHash(String id) {
		return this.jdbcTemplate.queryForObject("SELECT password_hash FROM users WHERE id = ?::uuid", String.class, id);
	}

	private boolean resetPasswordCodeExists(String id) {
		Integer count = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users " + "WHERE id = ?::uuid AND reset_password_code_hash IS NOT NULL",
				Integer.class, id);
		return count != null && count == 1;
	}

	private boolean resetPasswordExpiresAtExists(String id) {
		Integer count = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users " + "WHERE id = ?::uuid AND reset_password_expires_at IS NOT NULL",
				Integer.class, id);
		return count != null && count == 1;
	}

	private String userStatus(String id) {
		return this.jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, id);
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
