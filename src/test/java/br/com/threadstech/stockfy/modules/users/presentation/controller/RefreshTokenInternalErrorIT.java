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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.usecase.RefreshTokenUseCase;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "server.error.include-stacktrace=never")
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RefreshTokenInternalErrorIT {

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TokenService tokenService;

	@MockitoBean
	private RefreshTokenUseCase refreshTokenUseCase;

	@Test
	@DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
	void refreshToken_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() {
		long usersBeforeRequest = countUsers();
		UserSnapshot userBeforeRequest = userSnapshot();
		String refreshToken = tokenService.generateRefreshToken(USER_ID);
		doThrow(new RuntimeException("refresh database stacktrace detail")).when(refreshTokenUseCase)
			.execute(refreshToken);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.COOKIE, "refresh_token=" + refreshToken);

		ResponseEntity<String> response = restTemplate.exchange("/api/v1/auth/sessions/refresh", HttpMethod.POST,
				new HttpEntity<>(headers), String.class);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertThat(response.getBody(), not(containsString("refresh database stacktrace detail")));
		assertThat(response.getBody(), not(containsString("RuntimeException")));
		assertThat(response.getBody(), not(containsString("at ")));
		assertThat(response.getBody(), not(containsString(".java:")));
		assertThat(response.getHeaders().toString(), not(containsString("access_token")));
		assertThat(response.getHeaders().toString(), not(containsString("refresh_token")));
		assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
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

	private record UserSnapshot(Map<String, Object> fields, List<String> roles) {
	}

}
