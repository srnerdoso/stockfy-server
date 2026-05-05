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

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
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
import org.springframework.test.web.servlet.MvcResult;

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
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.AvoidDuplicateLiterals" })
class RefreshTokenIT {

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

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

	@AfterEach
	void tearDown() {
		clearRateLimitBuckets();
		this.rateLimitTimeMeter.reset();
	}

	@Test
	@DisplayName("Deve retornar 200 com novos cookies e rotacionar refresh token")
	void refreshToken_whenRefreshTokenIsValid_thenReturns200WithNewCookiesAndRotatesToken() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String oldRefreshToken = this.tokenService.generateRefreshToken(USER_ID);

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", oldRefreshToken)))
			.andExpect(status().isOk())
			.andExpect(content().string(""))
			.andExpect(cookie().exists("access_token"))
			.andExpect(cookie().exists("refresh_token"))
			.andExpect(cookie().httpOnly("access_token", true))
			.andExpect(cookie().httpOnly("refresh_token", true))
			.andReturn();

		Cookie newRefreshCookie = result.getResponse().getCookie("refresh_token");
		assertThat(newRefreshCookie).isNotNull();
		assertThat(newRefreshCookie.getValue()).isNotEqualTo(oldRefreshToken);
		assertThat(this.redisTemplate.hasKey("refresh_token:" + oldRefreshToken)).isFalse();
		assertThat(this.redisTemplate.hasKey("refresh_token:" + newRefreshCookie.getValue())).isTrue();
		MatcherAssert.assertThat(result.getResponse().getHeaders("Set-Cookie"), everyItem(containsString("Secure")));
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token estiver ausente")
	void refreshToken_whenRefreshTokenIsMissing_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		MvcResult result = this.mockMvc.perform(post("/api/v1/auth/sessions/refresh"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token for invalido")
	void refreshToken_whenRefreshTokenIsInvalid_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", "invalid-refresh-token")))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando refresh token estiver em branco")
	void refreshToken_whenRefreshTokenIsBlank_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", "")))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
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

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", revokedRefreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
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

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		assertThat(this.redisTemplate.hasKey("refresh_token:" + refreshToken)).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando valor do Redis estiver malformado")
	void refreshToken_whenRedisValueIsMalformed_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = "malformed-refresh-token";
		this.redisTemplate.opsForValue().set("refresh_token:" + refreshToken, "not-a-uuid");

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		assertThat(this.redisTemplate.hasKey("refresh_token:" + refreshToken)).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario estiver bloqueado")
	void refreshToken_whenUserIsLocked_thenReturns401WithoutBody() throws Exception {
		this.jdbcTemplate.update("UPDATE users SET status = 'LOCKED' WHERE id = ?::uuid", USER_ID);
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		assertThat(this.redisTemplate.hasKey("refresh_token:" + refreshToken)).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario estiver inativo")
	void refreshToken_whenUserIsInactive_thenReturns401WithoutBody() throws Exception {
		this.jdbcTemplate.update("UPDATE users SET active = FALSE WHERE id = ?::uuid", USER_ID);
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = this.tokenService.generateRefreshToken(USER_ID);

		MvcResult result = this.mockMvc
			.perform(post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		assertThat(this.redisTemplate.hasKey("refresh_token:" + refreshToken)).isFalse();
		assertNoSensitiveData(result);
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	void refreshToken_whenGeneralLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < 10; i++) {
			this.mockMvc.perform(authenticatedRefreshRequest(this.tokenService.generateRefreshToken(USER_ID)))
				.andExpect(status().isOk());
		}

		String blockedRefreshToken = this.tokenService.generateRefreshToken(USER_ID);
		this.mockMvc.perform(authenticatedRefreshRequest(blockedRefreshToken)).andExpect(status().isTooManyRequests());

		assertThat(this.redisTemplate.hasKey("refresh_token:" + blockedRefreshToken)).isTrue();
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
	}

	@Test
	@DisplayName("Deve permitir refresh quando a janela de rate limit expirar")
	void refreshToken_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();

		for (int i = 0; i < 10; i++) {
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

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedRefreshRequest(
			String refreshToken) {
		return post("/api/v1/auth/sessions/refresh").cookie(new Cookie("refresh_token", refreshToken));
	}

	private void assertNoSensitiveData(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString();
		MatcherAssert.assertThat(body, not(containsString("password")));
		MatcherAssert.assertThat(body, not(containsString("password_hash")));
		MatcherAssert.assertThat(body, not(containsString("resetCode")));
		MatcherAssert.assertThat(body, not(containsString("resetPasswordCodeHash")));
		MatcherAssert.assertThat(body, not(containsString("access_token")));
		MatcherAssert.assertThat(body, not(containsString("refresh_token")));
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

	private record UserSnapshot(Map<String, Object> fields, List<String> roles) {
	}

}
