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

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitBucketCleaner;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
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
import org.springframework.test.web.servlet.ResultActions;

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
class UpdatePasswordIT {

	private static final int JSON_OBJECT_START_LENGTH = 1;

	private static final String PASSWORD_ENDPOINT = "/api/v1/users/password";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String DEFAULT_RESET_CODE = "123456";

	private static final String INVALID_RESET_CODE = "999999";

	private static final String NEW_PASSWORD = "newPassword123";

	private static final String ANOTHER_PASSWORD = "anotherPassword123";

	private static final String DIFFERENT_PASSWORD = "differentPassword123";

	private static final String OLD_PASSWORD = "oldPassword123";

	private static final String WRONG_PASSWORD = "wrongPassword123";

	private static final String SQL_INJECTION_CODE = "123456' OR '1'='1";

	private static final int PASSWORD_RATE_LIMIT = 5;

	private static final String USER_ROLE = "USER";

	private static final String ERROR_TYPE_PATH = "$.type";

	private static final String ERROR_TITLE_PATH = "$.title";

	private static final String ERROR_STATUS_PATH = "$.status";

	private static final String ERROR_DETAIL_PATH = "$.detail";

	private static final String ERROR_INSTANCE_PATH = "$.instance";

	private static final String ERROR_FIELD_ERRORS_PATH = "$.fieldErrors";

	private static final String UNPROCESSABLE_ENTITY = "Unprocessable Entity";

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
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
	}

	@Test
	@DisplayName("Deve retornar 204, atualizar senha e revogar codigo quando code for valido")
	void updatePassword_whenResetCodeIsValid_thenReturns204UpdatesPasswordAndRevokesCode() throws Exception {
		configureResetCode(OWNER_ID, DEFAULT_RESET_CODE, LocalDateTime.now().plusHours(1));
		String oldHash = passwordHash(OWNER_ID);

		passwordRequest(passwordResetBody(DEFAULT_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		String newHash = passwordHash(OWNER_ID);
		assertThat(newHash).isNotEqualTo(oldHash);
		assertThat(this.passwordEncoder.matches(NEW_PASSWORD, newHash)).isTrue();
		assertThat(resetPasswordCodeExists(OWNER_ID)).isFalse();
		assertThat(resetPasswordExpiresAtExists(OWNER_ID)).isFalse();
	}

	@Test
	@DisplayName("Deve retornar 422 quando tentar reutilizar o mesmo code")
	void updatePassword_whenResetCodeIsReused_thenReturns422AndDoesNotAlterPassword() throws Exception {
		configureResetCode(OWNER_ID, DEFAULT_RESET_CODE, LocalDateTime.now().plusHours(1));

		passwordRequest(passwordResetBody(DEFAULT_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isNoContent());
		String hashAfterFirstUse = passwordHash(OWNER_ID);

		passwordRequest(passwordResetBody(DEFAULT_RESET_CODE, ANOTHER_PASSWORD, ANOTHER_PASSWORD))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath(ERROR_TYPE_PATH).value(aboutBlank()))
			.andExpect(jsonPath(ERROR_TITLE_PATH).value(UNPROCESSABLE_ENTITY))
			.andExpect(jsonPath(ERROR_STATUS_PATH).value(422))
			.andExpect(jsonPath(ERROR_DETAIL_PATH).value("Código de recuperação inválido ou expirado."))
			.andExpect(jsonPath(ERROR_INSTANCE_PATH).value(PASSWORD_ENDPOINT))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH).doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(hashAfterFirstUse);
	}

	@Test
	@DisplayName("Deve retornar 422 quando code estiver expirado")
	void updatePassword_whenResetCodeIsExpired_thenReturns422AndDoesNotAlterPassword() throws Exception {
		configureResetCode(OWNER_ID, DEFAULT_RESET_CODE, LocalDateTime.now().minusMinutes(1));
		String oldHash = passwordHash(OWNER_ID);

		passwordRequest(passwordResetBody(DEFAULT_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath(ERROR_TYPE_PATH).value(aboutBlank()))
			.andExpect(jsonPath(ERROR_TITLE_PATH).value(UNPROCESSABLE_ENTITY))
			.andExpect(jsonPath(ERROR_STATUS_PATH).value(422))
			.andExpect(jsonPath(ERROR_DETAIL_PATH).value("Código de recuperação inválido ou expirado."))
			.andExpect(jsonPath(ERROR_INSTANCE_PATH).value(PASSWORD_ENDPOINT))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH).doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 204 quando usuario autenticado informar senha atual correta")
	@WithMockUserId(id = OWNER_ID, roles = USER_ROLE)
	void updatePassword_whenAuthenticatedAndCurrentPasswordIsCorrect_thenReturns204() throws Exception {
		configurePassword(OWNER_ID, OLD_PASSWORD);

		passwordRequest(authenticatedPasswordBody(OLD_PASSWORD, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(this.passwordEncoder.matches(NEW_PASSWORD, passwordHash(OWNER_ID))).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando code nao for enviado e usuario nao autenticado")
	void updatePassword_whenCodeIsMissingAndUserIsNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		passwordRequest(passwordResetBody(null, NEW_PASSWORD, NEW_PASSWORD)).andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 400 quando newPassword for omitido")
	void updatePassword_whenNewPasswordIsMissing_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				passwordRequest(passwordResetBody(DEFAULT_RESET_CODE, null, NEW_PASSWORD)), "newPassword",
				"A senha é obrigatória.");

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 400 quando code contiver SQL Injection")
	void updatePassword_whenCodeContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				passwordRequest(passwordResetBody(SQL_INJECTION_CODE, NEW_PASSWORD, NEW_PASSWORD)), "code",
				"O código de recuperação é inválido.");

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 422 quando senhas nao coincidirem")
	void updatePassword_whenPasswordsDoNotMatch_thenReturns422AndDoesNotAlterData() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		passwordRequest(passwordResetBody(DEFAULT_RESET_CODE, NEW_PASSWORD, DIFFERENT_PASSWORD))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath(ERROR_TYPE_PATH).value(aboutBlank()))
			.andExpect(jsonPath(ERROR_TITLE_PATH).value(UNPROCESSABLE_ENTITY))
			.andExpect(jsonPath(ERROR_STATUS_PATH).value(422))
			.andExpect(jsonPath(ERROR_DETAIL_PATH).value("As senhas não coincidem."))
			.andExpect(jsonPath(ERROR_INSTANCE_PATH).value(PASSWORD_ENDPOINT))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH + ".length()").value(1))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH + "[0].field").value("confirmPassword"))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH + "[0].message").value("As senhas não coincidem."));

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 422 quando senha atual for incorreta")
	@WithMockUserId(id = OWNER_ID, roles = USER_ROLE)
	void updatePassword_whenCurrentPasswordIsWrong_thenReturns422AndDoesNotAlterData() throws Exception {
		configurePassword(OWNER_ID, OLD_PASSWORD);
		String oldHash = passwordHash(OWNER_ID);

		passwordRequest(authenticatedPasswordBody(WRONG_PASSWORD, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath(ERROR_TYPE_PATH).value(aboutBlank()))
			.andExpect(jsonPath(ERROR_TITLE_PATH).value(UNPROCESSABLE_ENTITY))
			.andExpect(jsonPath(ERROR_STATUS_PATH).value(422))
			.andExpect(jsonPath(ERROR_DETAIL_PATH).value("Senha atual incorreta."))
			.andExpect(jsonPath(ERROR_INSTANCE_PATH).value(PASSWORD_ENDPOINT))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH).doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve retornar 422 quando senha atual for omitida no fluxo autenticado")
	@WithMockUserId(id = OWNER_ID, roles = USER_ROLE)
	void updatePassword_whenCurrentPasswordIsMissing_thenReturns422AndDoesNotAlterData() throws Exception {
		configurePassword(OWNER_ID, OLD_PASSWORD);
		String oldHash = passwordHash(OWNER_ID);

		passwordRequest(authenticatedPasswordBody(null, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath(ERROR_TYPE_PATH).value(aboutBlank()))
			.andExpect(jsonPath(ERROR_TITLE_PATH).value(UNPROCESSABLE_ENTITY))
			.andExpect(jsonPath(ERROR_STATUS_PATH).value(422))
			.andExpect(jsonPath(ERROR_DETAIL_PATH).value("Senha atual incorreta."))
			.andExpect(jsonPath(ERROR_INSTANCE_PATH).value(PASSWORD_ENDPOINT))
			.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH).doesNotExist());

		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve bloquear usuario autenticado apos 15 tentativas invalidas")
	@WithMockUserId(id = OWNER_ID, roles = USER_ROLE)
	void updatePassword_whenCurrentPasswordFailsFifteenTimes_thenLocksUser() throws Exception {
		configurePassword(OWNER_ID, OLD_PASSWORD);
		String originalPasswordHash = passwordHash(OWNER_ID);

		for (int i = 0; i < 15; i++) {
			passwordRequest(authenticatedPasswordBody(WRONG_PASSWORD, NEW_PASSWORD, NEW_PASSWORD))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath(ERROR_TYPE_PATH).value(aboutBlank()))
				.andExpect(jsonPath(ERROR_TITLE_PATH).value(UNPROCESSABLE_ENTITY))
				.andExpect(jsonPath(ERROR_STATUS_PATH).value(422))
				.andExpect(jsonPath(ERROR_DETAIL_PATH).value("Senha atual incorreta."))
				.andExpect(jsonPath(ERROR_INSTANCE_PATH).value(PASSWORD_ENDPOINT))
				.andExpect(jsonPath(ERROR_FIELD_ERRORS_PATH).doesNotExist());

			assertThat(passwordHash(OWNER_ID)).isEqualTo(originalPasswordHash);

			if ((i + 1) % PASSWORD_RATE_LIMIT == 0) {
				this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));
			}
		}

		assertThat(userStatus(OWNER_ID)).isEqualTo("LOCKED");
		assertThat(passwordHash(OWNER_ID)).isEqualTo(originalPasswordHash);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite de 5 requisicoes por minuto")
	void updatePassword_whenLimitExceeded_thenReturns429() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		for (int i = 0; i < PASSWORD_RATE_LIMIT; i++) {
			passwordRequest(passwordResetBody(INVALID_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
				.andExpect(status().isUnprocessableEntity());
		}
		passwordRequest(passwordResetBody(INVALID_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isTooManyRequests());
		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	@Test
	@DisplayName("Deve permitir nova tentativa quando a janela de rate limit expirar")
	void updatePassword_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		String oldHash = passwordHash(OWNER_ID);

		for (int i = 0; i < PASSWORD_RATE_LIMIT; i++) {
			passwordRequest(passwordResetBody(INVALID_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
				.andExpect(status().isUnprocessableEntity());
		}

		passwordRequest(passwordResetBody(INVALID_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isTooManyRequests());
		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		passwordRequest(passwordResetBody(INVALID_RESET_CODE, NEW_PASSWORD, NEW_PASSWORD))
			.andExpect(status().isUnprocessableEntity());
		assertThat(passwordHash(OWNER_ID)).isEqualTo(oldHash);
	}

	private ResultActions passwordRequest(String body) throws Exception {
		return this.mockMvc.perform(patch(PASSWORD_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private String aboutBlank() {
		return "about:blank";
	}

	private String passwordResetBody(String code, String newPassword, String confirmPassword) {
		StringBuilder body = new StringBuilder("{");
		appendJsonField(body, "code", code);
		appendJsonField(body, "newPassword", newPassword);
		appendJsonField(body, "confirmPassword", confirmPassword);
		body.append("}");
		return body.toString();
	}

	private String authenticatedPasswordBody(String currentPassword, String newPassword, String confirmPassword) {
		StringBuilder body = new StringBuilder("{");
		appendJsonField(body, "currentPassword", currentPassword);
		appendJsonField(body, "newPassword", newPassword);
		appendJsonField(body, "confirmPassword", confirmPassword);
		body.append("}");
		return body.toString();
	}

	private void appendJsonField(StringBuilder body, String fieldName, String value) {
		if (value == null) {
			return;
		}
		if (body.length() > JSON_OBJECT_START_LENGTH) {
			body.append(",");
		}
		body.append("\"").append(fieldName).append("\":\"").append(value).append("\"");
	}

	private void configureResetCode(String id, String rawCode, LocalDateTime expiresAt) {
		this.jdbcTemplate.update(
				"UPDATE users SET reset_password_code_hash = ?, reset_password_expires_at = ? WHERE id = ?::uuid",
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
				"SELECT COUNT(*) FROM users WHERE id = ?::uuid AND reset_password_code_hash IS NOT NULL", Integer.class,
				id);
		return count != null && count == 1;
	}

	private boolean resetPasswordExpiresAtExists(String id) {
		Integer count = this.jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users WHERE id = ?::uuid AND reset_password_expires_at IS NOT NULL",
				Integer.class, id);
		return count != null && count == 1;
	}

	private String userStatus(String id) {
		return this.jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, id);
	}

}
