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
import jakarta.servlet.http.Cookie;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LoginUserIT {

	private static final int LOGIN_RATE_LIMIT = 5;

	private static final int JSON_OBJECT_START_LENGTH = 1;

	private static final String AUTH_SESSIONS_ENDPOINT = "/api/v1/auth/sessions";

	private static final String ACCESS_TOKEN_COOKIE = "access_token";

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private static final String LOGIN_ATTEMPTS_KEY_PREFIX = "login_attempts:";

	private static final String USER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String USER_EMAIL = "bruno.user@example.com";

	private static final String RAW_PASSWORD = "password123";

	private static final String WRONG_PASSWORD = "wrongPassword123";

	private static final String MISSING_USER_EMAIL = "missing.user@example.com";

	private static final String ACTIVE_STATUS = "ACTIVE";

	private static final String EMAIL_FIELD = "email";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@BeforeEach
	void setPassword() {
		this.jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?::uuid",
				this.passwordEncoder.encode(RAW_PASSWORD), USER_ID);
	}

	@AfterEach
	void tearDown() {
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
		this.redisTemplate.delete(loginAttemptsKey(USER_EMAIL));
	}

	@Test
	@DisplayName("Deve retornar 200 com cookies seguros e HttpOnly quando credenciais forem validas")
	void loginUser_whenCredentialsAreValid_thenReturns200WithSecureHttpOnlyCookies() throws Exception {
		long usersBeforeRequest = countUsers();

		var result = loginRequest(validBody()).andExpect(status().isOk())
			.andExpect(content().string(""))
			.andExpect(cookie().exists(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().exists(REFRESH_TOKEN_COOKIE))
			.andExpect(cookie().httpOnly(ACCESS_TOKEN_COOKIE, true))
			.andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE, true))
			.andReturn();

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
		MatcherAssert.assertThat(result.getResponse().getHeaders("Set-Cookie").get(0), containsString("Secure"));
		MatcherAssert.assertThat(result.getResponse().getHeaders("Set-Cookie").get(1), containsString("Secure"));
		Cookie refreshToken = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
		assertThat(refreshToken).isNotNull();
		assertThat(Boolean.TRUE.equals(this.redisTemplate.hasKey(refreshTokenKey(refreshToken.getValue())))).isTrue();
		assertThat(this.redisTemplate.opsForValue().get(loginAttemptsKey(USER_EMAIL))).isNull();
	}

	@Test
	@DisplayName("Deve retornar 400 quando email for omitido")
	void loginUser_whenEmailIsMissing_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(loginRequest(loginBody(null, RAW_PASSWORD)),
				EMAIL_FIELD, "O e-mail é obrigatório.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve retornar 400 quando email for invalido")
	void loginUser_whenEmailIsInvalid_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				loginRequest(loginBody("invalid-email", RAW_PASSWORD)), EMAIL_FIELD, "O e-mail informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve retornar 400 com todos os erros quando email e senha forem omitidos")
	void loginUser_whenEmailAndPasswordAreMissing_thenReturns400WithBothFieldErrors() throws Exception {
		loginRequest("{}").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.detail").value("Erro de validação nos campos informados."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors.length()").value(2))
			.andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem(EMAIL_FIELD)))
			.andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("password")))
			.andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("O e-mail é obrigatório.")))
			.andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("A senha é obrigatória.")));
	}

	@Test
	@DisplayName("Deve retornar 400 quando senha for omitida")
	void loginUser_whenPasswordIsMissing_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(loginRequest(loginBody(USER_EMAIL, null)),
				"password", "A senha é obrigatória.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve retornar 400 quando email contiver SQL Injection")
	void loginUser_whenEmailContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				loginRequest(loginBody("user@example.com' OR '1'='1", RAW_PASSWORD)), EMAIL_FIELD,
				"O e-mail informado é inválido.");

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve retornar 401 sem dados sensiveis quando senha estiver incorreta")
	void loginUser_whenPasswordIsWrong_thenReturns401WithoutSensitiveData() throws Exception {
		long usersBeforeRequest = countUsers();

		var result = loginRequest(loginBody(USER_EMAIL, WRONG_PASSWORD)).andExpect(status().isUnauthorized())
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		String response = result.getResponse().getContentAsString();
		MatcherAssert.assertThat(response, not(containsString(WRONG_PASSWORD)));
		MatcherAssert.assertThat(response, not(containsString("password-hash")));
		MatcherAssert.assertThat(response, not(containsString(ACCESS_TOKEN_COOKIE)));
		MatcherAssert.assertThat(response, not(containsString(REFRESH_TOKEN_COOKIE)));
		MatcherAssert.assertThat(response, not(containsString("RuntimeException")));
		MatcherAssert.assertThat(response, not(containsString(".java:")));
		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve retornar 401 sem dados sensiveis quando email nao existir")
	void loginUser_whenEmailDoesNotExist_thenReturns401WithoutSensitiveData() throws Exception {
		long usersBeforeRequest = countUsers();

		var result = loginRequest(loginBody(MISSING_USER_EMAIL, RAW_PASSWORD)).andExpect(status().isUnauthorized())
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		String response = result.getResponse().getContentAsString();
		MatcherAssert.assertThat(response, not(containsString(MISSING_USER_EMAIL)));
		MatcherAssert.assertThat(response, not(containsString(RAW_PASSWORD)));
		MatcherAssert.assertThat(response, not(containsString("password-hash")));
		MatcherAssert.assertThat(response, not(containsString(ACCESS_TOKEN_COOKIE)));
		MatcherAssert.assertThat(response, not(containsString(REFRESH_TOKEN_COOKIE)));
		MatcherAssert.assertThat(response, not(containsString("RuntimeException")));
		MatcherAssert.assertThat(response, not(containsString(".java:")));
		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve retornar 401 quando senha contiver SQL Injection")
	void loginUser_whenPasswordContainsSqlInjection_thenReturns401AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();
		String sqlInjectionPassword = "' OR '1'='1";

		var result = loginRequest(loginBody(USER_EMAIL, sqlInjectionPassword)).andExpect(status().isUnauthorized())
			.andReturn();

		MatcherAssert.assertThat(result.getResponse().getContentAsString(), not(containsString(sqlInjectionPassword)));
		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve bloquear usuario na decima quinta tentativa invalida")
	void loginUser_whenFifteenthInvalidAttempt_thenLocksUser() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < LOGIN_RATE_LIMIT; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
				.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));
		}
		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));
		for (int i = 0; i < LOGIN_RATE_LIMIT; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
				.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));
		}
		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));
		for (int i = 0; i < LOGIN_RATE_LIMIT; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
				.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));
		}

		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userStatus()).isEqualTo("LOCKED");
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite de login")
	void loginUser_whenLimitExceeded_thenReturns429() throws Exception {
		for (int i = 0; i < LOGIN_RATE_LIMIT; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
				.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));
		}
		performWrongPasswordLogin().andExpect(status().isTooManyRequests())
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));

		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Deve processar login quando janela de rate limit expirar")
	void loginUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		for (int i = 0; i < LOGIN_RATE_LIMIT; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
				.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));
		}

		performWrongPasswordLogin().andExpect(status().isTooManyRequests())
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		loginRequest(validBody()).andExpect(status().isOk()).andExpect(content().string(""));
		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	@Test
	@DisplayName("Nao deve compartilhar rate limit entre IPs diferentes")
	void loginUser_whenDifferentClientIp_thenDoesNotShareRateLimitBucket() throws Exception {
		RequestPostProcessor firstClient = remoteAddress("10.0.0.1");
		RequestPostProcessor secondClient = remoteAddress("10.0.0.2");

		for (int i = 0; i < LOGIN_RATE_LIMIT; i++) {
			this.mockMvc.perform(wrongPasswordRequest().with(firstClient))
				.andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
				.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));
		}

		this.mockMvc.perform(wrongPasswordRequest().with(firstClient))
			.andExpect(status().isTooManyRequests())
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));

		this.mockMvc.perform(wrongPasswordRequest().with(secondClient))
			.andExpect(status().isUnauthorized())
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE));

		assertThat(userStatus()).isEqualTo(ACTIVE_STATUS);
	}

	private org.springframework.test.web.servlet.ResultActions performWrongPasswordLogin() throws Exception {
		return this.mockMvc.perform(wrongPasswordRequest());
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder wrongPasswordRequest() {
		return post(AUTH_SESSIONS_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
			.content(loginBody(USER_EMAIL, WRONG_PASSWORD));
	}

	private RequestPostProcessor remoteAddress(String remoteAddress) {
		return (request) -> {
			request.setRemoteAddr(remoteAddress);
			return request;
		};
	}

	private String validBody() {
		return loginBody(USER_EMAIL, RAW_PASSWORD);
	}

	private org.springframework.test.web.servlet.ResultActions loginRequest(String body) throws Exception {
		return this.mockMvc.perform(post(AUTH_SESSIONS_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private String loginBody(String email, String password) {
		StringBuilder body = new StringBuilder("{");
		appendJsonField(body, "email", email);
		appendJsonField(body, "password", password);
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

	private String loginAttemptsKey(String email) {
		return LOGIN_ATTEMPTS_KEY_PREFIX + email;
	}

	private String refreshTokenKey(String token) {
		return REFRESH_TOKEN_COOKIE + ":" + token;
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private String userStatus() {
		return this.jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, USER_ID);
	}

}
