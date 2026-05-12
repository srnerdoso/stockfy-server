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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitBucketCleaner;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.HmacSha256Hasher;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/refresh-token-scenarios.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RefreshTokenIT {

	private static final int REFRESH_RATE_LIMIT = 5;

	private static final int REFRESH_BLOCK_LIMIT = 15;

	private static final String REFRESH_ENDPOINT = "/api/v1/auth/sessions/refresh";

	private static final String ACCESS_TOKEN_COOKIE = "access_token";

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private static final String INVALID_REFRESH_TOKEN = "invalid-refresh-token";

	private static final String MALFORMED_REFRESH_TOKEN = "malformed-refresh-token";

	private static final String MALFORMED_REDIS_VALUE = "not-a-uuid";

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private static final UUID LOCKED_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

	private static final UUID INACTIVE_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");

	@DynamicPropertySource
	static void registerLegacyResetHashSecret(DynamicPropertyRegistry registry) {
		registry.add("stockfy.users.reset-code-hash-secret", () -> "test-reset-code-hash-secret");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@Autowired
	private HmacSha256Hasher hmacSha256Hasher;

	@AfterEach
	void tearDown() {
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
	}

	@Test
	@DisplayName("Deve retornar 200 com novos cookies e rotacionar refresh token")
	void refreshToken_whenRefreshTokenIsValid_thenReturns200WithNewCookiesAndRotatesToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String oldRefreshToken = this.tokenService.generateRefreshToken(USER_ID);

		MvcResult result = this.mockMvc.perform(refreshRequest(oldRefreshToken))
			.andExpect(status().isOk())
			.andExpect(content().string(""))
			.andExpect(cookie().exists(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().exists(REFRESH_TOKEN_COOKIE))
			.andExpect(cookie().httpOnly(ACCESS_TOKEN_COOKIE, true))
			.andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE, true))
			.andReturn();

		Cookie newRefreshCookie = result.getResponse().getCookie(REFRESH_TOKEN_COOKIE);
		assertThat(newRefreshCookie).isNotNull();
		assertThat(newRefreshCookie.getValue()).isNotEqualTo(oldRefreshToken);
		assertThat(this.redisTemplate.hasKey(refreshTokenKey(oldRefreshToken))).isFalse();
		assertThat(this.redisTemplate.hasKey(refreshTokenKey(newRefreshCookie.getValue()))).isTrue();
		MatcherAssert.assertThat(result.getResponse().getHeaders("Set-Cookie"), everyItem(containsString("Secure")));
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token estiver ausente")
	void refreshToken_whenRefreshTokenIsMissing_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		MvcResult result = this.mockMvc.perform(post(REFRESH_ENDPOINT))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token for invalido")
	void refreshToken_whenRefreshTokenIsInvalid_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		MvcResult result = this.mockMvc.perform(refreshRequest(INVALID_REFRESH_TOKEN))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token estiver em branco")
	void refreshToken_whenRefreshTokenIsBlank_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		MvcResult result = this.mockMvc.perform(refreshRequest(""))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token ja foi revogado")
	void refreshToken_whenRefreshTokenWasRevoked_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String revokedRefreshToken = this.tokenService.generateRefreshToken(USER_ID);
		this.tokenService.revokeRefreshToken(revokedRefreshToken);

		MvcResult result = this.mockMvc.perform(refreshRequest(revokedRefreshToken))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario do refresh token nao existir")
	void refreshToken_whenUserDoesNotExist_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(UUID.randomUUID());

		MvcResult result = this.mockMvc.perform(refreshRequest(refreshToken))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando valor do Redis estiver malformado")
	void refreshToken_whenRedisValueIsMalformed_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = MALFORMED_REFRESH_TOKEN;
		this.redisTemplate.opsForValue().set(refreshTokenKey(refreshToken), MALFORMED_REDIS_VALUE);

		MvcResult result = this.mockMvc.perform(refreshRequest(refreshToken))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario estiver bloqueado")
	void refreshToken_whenUserIsLocked_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(LOCKED_USER_ID);

		MvcResult result = this.mockMvc.perform(refreshRequest(refreshToken))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario estiver inativo")
	void refreshToken_whenUserIsInactive_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(INACTIVE_USER_ID);

		MvcResult result = this.mockMvc.perform(refreshRequest(refreshToken))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist(ACCESS_TOKEN_COOKIE))
			.andExpect(cookie().doesNotExist(REFRESH_TOKEN_COOKIE))
			.andReturn();

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 429 na sexta requisicao de refresh token no mesmo minuto")
	void refreshToken_whenRefreshLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < REFRESH_RATE_LIMIT; i++) {
			this.mockMvc.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
				.andExpect(status().isOk());
		}

		String blockedRefreshToken = this.tokenService.generateRefreshToken(USER_ID);
		this.mockMvc.perform(authenticatedRefreshRequest(blockedRefreshToken)).andExpect(status().isTooManyRequests());

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(blockedRefreshToken))).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve bloquear refresh token apos quinze tentativas")
	void refreshToken_whenRefreshBlockLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < REFRESH_BLOCK_LIMIT; i++) {
			this.mockMvc.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
				.andExpect((i < REFRESH_RATE_LIMIT) ? status().isOk() : status().isTooManyRequests());
		}

		this.mockMvc.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
			.andExpect(status().isTooManyRequests());

		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve permitir refresh quando a janela de rate limit expirar")
	void refreshToken_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < REFRESH_RATE_LIMIT; i++) {
			this.mockMvc.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
				.andExpect(status().isOk());
		}

		this.mockMvc.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
			.andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		MvcResult result = this.mockMvc
			.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
			.andExpect(status().isOk())
			.andExpect(content().string(""))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	private MockHttpServletRequestBuilder authenticatedRefreshRequest(String refreshToken) {
		return refreshRequest(refreshToken);
	}

	private MockHttpServletRequestBuilder refreshRequest(String refreshToken) {
		return post(REFRESH_ENDPOINT).cookie(new Cookie(REFRESH_TOKEN_COOKIE, refreshToken));
	}

	private void assertNoSensitiveData(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString();
		MatcherAssert.assertThat(body, not(containsString("password")));
		MatcherAssert.assertThat(body, not(containsString("password_hash")));
		MatcherAssert.assertThat(body, not(containsString("resetCode")));
		MatcherAssert.assertThat(body, not(containsString("resetPasswordCodeHash")));
		MatcherAssert.assertThat(body, not(containsString(ACCESS_TOKEN_COOKIE)));
		MatcherAssert.assertThat(body, not(containsString(REFRESH_TOKEN_COOKIE)));
	}

	private long countUsers() {
		Long count = this.jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return (count != null) ? count : 0;
	}

	private UserSnapshot userSnapshot() {
		Map<String, Object> fields = this.jdbcTemplate.queryForMap("""
				SELECT
				    id,
				    name,
				    email,
				    password_hash,
				    status,
				    active,
				    reset_password_code_hash,
				    reset_password_expires_at,
				    created_at,
				    created_by,
				    updated_at,
				    updated_by
				FROM users
				WHERE id = ?::uuid
				""", USER_ID);
		List<String> roles = this.jdbcTemplate
			.queryForList("SELECT role FROM users_roles WHERE user_id = ?::uuid ORDER BY role", String.class, USER_ID);
		return new UserSnapshot(new TreeMap<>(fields), roles);
	}

	private void assertUserDataUnchanged(long usersBeforeRequest, UserSnapshot userBeforeRequest) {
		assertThat(countUsers()).isEqualTo(usersBeforeRequest);
		assertThat(userSnapshot()).isEqualTo(userBeforeRequest);
	}

	private String refreshTokenKey(String refreshToken) {
		return REFRESH_TOKEN_COOKIE + ":" + this.hmacSha256Hasher.hash(refreshToken);
	}

	private record UserSnapshot(Map<String, Object> fields, List<String> roles) {
	}

}
