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
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitBucketCleaner;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.HmacSha256Hasher;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LogoutUserIT {

	private static final int GENERAL_RATE_LIMIT = 10;

	private static final String CURRENT_SESSION_ENDPOINT = "/api/v1/auth/sessions/current";

	private static final String CURRENT_USER_ENDPOINT = "/api/v1/users/me";

	private static final String ACCESS_TOKEN_COOKIE = "access_token";

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private static final String INVALID_ACCESS_TOKEN = "invalid-access-token";

	private static final String INVALID_REFRESH_TOKEN = "invalid-refresh-token";

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JwtService jwtService;

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
	@DisplayName("Deve retornar 204, revogar refresh token e limpar cookies")
	void logoutUser_whenAuthenticated_thenReturns204RevokesTokenAndClearsCookies() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = this.jwtService.generateToken(USER_ID, Set.of(UserRole.USER));
		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);

		var result = this.mockMvc
			.perform(delete(CURRENT_SESSION_ENDPOINT).cookie(secureCookie(ACCESS_TOKEN_COOKIE, accessToken))
				.cookie(secureCookie(REFRESH_TOKEN_COOKIE, refreshToken)))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""))
			.andExpect(cookie().maxAge(ACCESS_TOKEN_COOKIE, 0))
			.andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE, 0))
			.andReturn();

		List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
		MatcherAssert.assertThat(setCookieHeaders,
				hasItem(allOf(containsString(ACCESS_TOKEN_COOKIE + "="), containsString("Max-Age=0"))));
		MatcherAssert.assertThat(setCookieHeaders,
				hasItem(allOf(containsString(REFRESH_TOKEN_COOKIE + "="), containsString("Max-Age=0"))));
		MatcherAssert.assertThat(setCookieHeaders, everyItem(containsString("Path=/")));
		MatcherAssert.assertThat(setCookieHeaders, everyItem(containsString("HttpOnly")));
		MatcherAssert.assertThat(setCookieHeaders, everyItem(containsString("Secure")));
		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve impedir nova autenticacao com os mesmos cookies apos logout")
	void logoutUser_whenSameCookiesAreReused_thenPrivateEndpointReturns401() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = this.jwtService.generateToken(USER_ID, Set.of(UserRole.USER));
		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);
		Cookie accessCookie = secureCookie(ACCESS_TOKEN_COOKIE, accessToken);
		Cookie refreshCookie = secureCookie(REFRESH_TOKEN_COOKIE, refreshToken);

		this.mockMvc.perform(delete(CURRENT_SESSION_ENDPOINT).cookie(accessCookie, refreshCookie))
			.andExpect(status().isNoContent());

		this.mockMvc.perform(get(CURRENT_USER_ENDPOINT).cookie(accessCookie, refreshCookie))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando nao autenticado")
	void logoutUser_whenCookiesAreMissing_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		this.mockMvc.perform(delete(CURRENT_SESSION_ENDPOINT))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token for invalido")
	void logoutUser_whenRefreshTokenIsInvalid_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = this.jwtService.generateToken(USER_ID, Set.of(UserRole.USER));

		this.mockMvc
			.perform(delete(CURRENT_SESSION_ENDPOINT).cookie(secureCookie(ACCESS_TOKEN_COOKIE, accessToken))
				.cookie(secureCookie(REFRESH_TOKEN_COOKIE, INVALID_REFRESH_TOKEN)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 quando access token estiver ausente")
	void logoutUser_whenAccessTokenIsMissing_thenReturns401AndKeepsRefreshToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);

		this.mockMvc.perform(delete(CURRENT_SESSION_ENDPOINT).cookie(secureCookie(REFRESH_TOKEN_COOKIE, refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 quando access token for invalido")
	void logoutUser_whenAccessTokenIsInvalid_thenReturns401AndKeepsRefreshToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);

		this.mockMvc
			.perform(delete(CURRENT_SESSION_ENDPOINT).cookie(secureCookie(ACCESS_TOKEN_COOKIE, INVALID_ACCESS_TOKEN))
				.cookie(secureCookie(REFRESH_TOKEN_COOKIE, refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 quando refresh token pertencer a outro usuario")
	void logoutUser_whenRefreshTokenBelongsToAnotherUser_thenReturns401AndKeepsToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = this.jwtService.generateToken(USER_ID, Set.of(UserRole.USER));
		String refreshToken = this.tokenService.generateRefreshToken(OTHER_USER_ID);

		this.mockMvc
			.perform(delete(CURRENT_SESSION_ENDPOINT).cookie(secureCookie(ACCESS_TOKEN_COOKIE, accessToken))
				.cookie(secureCookie(REFRESH_TOKEN_COOKIE, refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	void logoutUser_whenGeneralLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			this.mockMvc.perform(authenticatedLogoutRequest()).andExpect(status().isNoContent());
		}

		String blockedRefreshToken = this.tokenService.generateRefreshToken(USER_ID);
		this.mockMvc.perform(authenticatedLogoutRequest(blockedRefreshToken))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().string(""));

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(blockedRefreshToken))).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve permitir logout quando a janela de rate limit expirar")
	void logoutUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			this.mockMvc.perform(authenticatedLogoutRequest()).andExpect(status().isNoContent());
		}

		String blockedRefreshToken = this.tokenService.generateRefreshToken(USER_ID);
		this.mockMvc.perform(authenticatedLogoutRequest(blockedRefreshToken))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().string(""));

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);
		this.mockMvc.perform(authenticatedLogoutRequest(refreshToken))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertThat(this.redisTemplate.hasKey(refreshTokenKey(refreshToken))).isFalse();
		assertThat(this.redisTemplate.hasKey(refreshTokenKey(blockedRefreshToken))).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	private MockHttpServletRequestBuilder authenticatedLogoutRequest() {
		return authenticatedLogoutRequest(this.tokenService.generateRefreshToken(USER_ID));
	}

	private MockHttpServletRequestBuilder authenticatedLogoutRequest(String refreshToken) {
		return delete(CURRENT_SESSION_ENDPOINT)
			.cookie(secureCookie(ACCESS_TOKEN_COOKIE, this.jwtService.generateToken(USER_ID, Set.of(UserRole.USER))))
			.cookie(secureCookie(REFRESH_TOKEN_COOKIE, refreshToken));
	}

	private Cookie secureCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		return cookie;
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
