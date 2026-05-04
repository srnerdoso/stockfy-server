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

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import jakarta.servlet.http.Cookie;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LogoutUserIT {

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

	@AfterEach
	void tearDown() {
		clearRateLimitBuckets();
		rateLimitTimeMeter.reset();
	}

	@Test
	@DisplayName("Deve retornar 204, revogar refresh token e limpar cookies")
	void logoutUser_whenAuthenticated_thenReturns204RevokesTokenAndClearsCookies() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = jwtService.generateToken(USER_ID, Set.of(UserRole.USER));
		String refreshToken = tokenService.generateRefreshToken(USER_ID);

		var result = mockMvc
			.perform(delete("/api/v1/auth/sessions/current").cookie(new Cookie("access_token", accessToken))
				.cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""))
			.andExpect(cookie().maxAge("access_token", 0))
			.andExpect(cookie().maxAge("refresh_token", 0))
			.andReturn();

		List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
		assertThat(setCookieHeaders, hasItem(allOf(containsString("access_token="), containsString("Max-Age=0"))));
		assertThat(setCookieHeaders, hasItem(allOf(containsString("refresh_token="), containsString("Max-Age=0"))));
		assertThat(setCookieHeaders, everyItem(containsString("Path=/")));
		assertThat(setCookieHeaders, everyItem(containsString("HttpOnly")));
		assertThat(setCookieHeaders, everyItem(containsString("Secure")));
		assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve impedir nova autenticacao com os mesmos cookies apos logout")
	void logoutUser_whenSameCookiesAreReused_thenPrivateEndpointReturns401() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = jwtService.generateToken(USER_ID, Set.of(UserRole.USER));
		String refreshToken = tokenService.generateRefreshToken(USER_ID);
		Cookie accessCookie = new Cookie("access_token", accessToken);
		Cookie refreshCookie = new Cookie("refresh_token", refreshToken);

		mockMvc.perform(delete("/api/v1/auth/sessions/current").cookie(accessCookie, refreshCookie))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/users/me").cookie(accessCookie, refreshCookie))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando nao autenticado")
	void logoutUser_whenCookiesAreMissing_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		mockMvc.perform(delete("/api/v1/auth/sessions/current"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token for invalido")
	void logoutUser_whenRefreshTokenIsInvalid_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = jwtService.generateToken(USER_ID, Set.of(UserRole.USER));

		mockMvc
			.perform(delete("/api/v1/auth/sessions/current").cookie(new Cookie("access_token", accessToken))
				.cookie(new Cookie("refresh_token", "invalid-refresh-token")))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 quando access token estiver ausente")
	void logoutUser_whenAccessTokenIsMissing_thenReturns401AndKeepsRefreshToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = tokenService.generateRefreshToken(USER_ID);

		mockMvc.perform(delete("/api/v1/auth/sessions/current").cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 quando access token for invalido")
	void logoutUser_whenAccessTokenIsInvalid_thenReturns401AndKeepsRefreshToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = tokenService.generateRefreshToken(USER_ID);

		mockMvc
			.perform(delete("/api/v1/auth/sessions/current").cookie(new Cookie("access_token", "invalid-access-token"))
				.cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 quando refresh token pertencer a outro usuario")
	void logoutUser_whenRefreshTokenBelongsToAnotherUser_thenReturns401AndKeepsToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String accessToken = jwtService.generateToken(USER_ID, Set.of(UserRole.USER));
		String refreshToken = tokenService.generateRefreshToken(OTHER_USER_ID);

		mockMvc
			.perform(delete("/api/v1/auth/sessions/current").cookie(new Cookie("access_token", accessToken))
				.cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	void logoutUser_whenGeneralLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < 10; i++) {
			mockMvc.perform(authenticatedLogoutRequest()).andExpect(status().isNoContent());
		}

		String blockedRefreshToken = tokenService.generateRefreshToken(USER_ID);
		mockMvc.perform(authenticatedLogoutRequest(blockedRefreshToken))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().string(""));

		assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + blockedRefreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve permitir logout quando a janela de rate limit expirar")
	void logoutUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < 10; i++) {
			mockMvc.perform(authenticatedLogoutRequest()).andExpect(status().isNoContent());
		}

		String blockedRefreshToken = tokenService.generateRefreshToken(USER_ID);
		mockMvc.perform(authenticatedLogoutRequest(blockedRefreshToken))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().string(""));

		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		String refreshToken = tokenService.generateRefreshToken(USER_ID);
		mockMvc.perform(authenticatedLogoutRequest(refreshToken))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken)));
		assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + blockedRefreshToken)));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	private MockHttpServletRequestBuilder authenticatedLogoutRequest() {
		return authenticatedLogoutRequest(tokenService.generateRefreshToken(USER_ID));
	}

	private MockHttpServletRequestBuilder authenticatedLogoutRequest(String refreshToken) {
		return delete("/api/v1/auth/sessions/current")
			.cookie(new Cookie("access_token", jwtService.generateToken(USER_ID, Set.of(UserRole.USER))))
			.cookie(new Cookie("refresh_token", refreshToken));
	}

	private long countUsers() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return count == null ? 0 : count;
	}

	private UserSnapshot userSnapshot() {
		Map<String, Object> fields = jdbcTemplate.queryForMap("""
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
		List<String> roles = jdbcTemplate
			.queryForList("SELECT role FROM users_roles WHERE user_id = ?::uuid ORDER BY role", String.class, USER_ID);
		return new UserSnapshot(new TreeMap<>(fields), roles);
	}

	private void assertUserDataUnchanged(long usersBeforeRequest, UserSnapshot userBeforeRequest) {
		assertEquals(usersBeforeRequest, countUsers());
		assertEquals(userBeforeRequest, userSnapshot());
	}

	private void clearRateLimitBuckets() {
		try {
			clearBucketMap("loginBuckets");
			clearBucketMap("generalBuckets");
			clearBucketMap("passwordBuckets");
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

	private record UserSnapshot(Map<String, Object> fields, List<String> roles) {
	}

}
