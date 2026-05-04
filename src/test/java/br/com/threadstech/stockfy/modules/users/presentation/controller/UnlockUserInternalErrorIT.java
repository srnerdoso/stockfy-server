package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.usecase.UnlockUserUseCase;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import java.util.Set;
import java.util.UUID;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "server.error.include-stacktrace=never")
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UnlockUserInternalErrorIT {

	private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private static final UUID LOCKED_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private TokenService tokenService;

	@MockitoBean
	private UnlockUserUseCase unlockUserUseCase;

	@Test
	@DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
	void unlockUser_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() {
		String oldStatus = userStatus();
		doThrow(new RuntimeException("unlock database stacktrace detail")).when(unlockUserUseCase).execute(LOCKED_ID);

		ResponseEntity<String> response = restTemplate.exchange("/api/v1/users/{id}/unlock", HttpMethod.PATCH,
				new HttpEntity<>(authenticatedHeaders()), String.class, LOCKED_ID);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertThat(response.getBody(), not(containsString("unlock database stacktrace detail")));
		assertThat(response.getBody(), not(containsString("RuntimeException")));
		assertThat(response.getBody(), not(containsString("at ")));
		assertThat(response.getBody(), not(containsString(".java:")));
		assertEquals(oldStatus, userStatus());
	}

	private HttpHeaders authenticatedHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.COOKIE, "access_token=" + jwtService.generateToken(ADMIN_ID, Set.of(UserRole.ADMIN)));
		headers.add(HttpHeaders.COOKIE, "refresh_token=" + tokenService.generateRefreshToken(ADMIN_ID));
		return headers;
	}

	private String userStatus() {
		return jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, LOCKED_ID);
	}

}
