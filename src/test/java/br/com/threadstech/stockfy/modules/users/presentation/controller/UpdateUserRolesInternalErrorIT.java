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
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.usecase.UpdateUserRolesUseCase;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import org.hamcrest.MatcherAssert;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "server.error.include-stacktrace=never")
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UpdateUserRolesInternalErrorIT {

	private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private TokenService tokenService;

	@MockitoBean
	private UpdateUserRolesUseCase updateUserRolesUseCase;

	@Test
	@DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
	void updateUserRoles_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() {
		List<String> oldRoles = userRoles();
		willThrow(new RuntimeException("roles database stacktrace detail")).given(this.updateUserRolesUseCase)
			.execute(USER_ID, Set.of(UserRole.ADMIN), Set.of(UserRole.USER));

		ResponseEntity<String> response = this.restTemplate.exchange("/api/v1/users/{id}/roles", HttpMethod.PATCH,
				new HttpEntity<>("{\"add\":[\"ADMIN\"],\"remove\":[\"USER\"]}", authenticatedHeaders()), String.class,
				USER_ID);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		MatcherAssert.assertThat(response.getBody(), not(containsString("roles database stacktrace detail")));
		MatcherAssert.assertThat(response.getBody(), not(containsString("RuntimeException")));
		MatcherAssert.assertThat(response.getBody(), not(containsString("at ")));
		MatcherAssert.assertThat(response.getBody(), not(containsString(".java:")));
		assertThat(userRoles()).isEqualTo(oldRoles);
	}

	private HttpHeaders authenticatedHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.COOKIE,
				"access_token=" + this.jwtService.generateToken(ADMIN_ID, Set.of(UserRole.ADMIN)));
		headers.add(HttpHeaders.COOKIE, "refresh_token=" + this.tokenService.generateRefreshToken(ADMIN_ID));
		return headers;
	}

	private List<String> userRoles() {
		return this.jdbcTemplate.queryForList("SELECT role FROM users_roles WHERE user_id = ? ORDER BY role",
				String.class, USER_ID);
	}

}
