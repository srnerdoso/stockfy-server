package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.application.usecase.FindUserByIdUseCase;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.TokenService;
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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "server.error.include-stacktrace=never")
@Import({TestcontainersConfiguration.class, RateLimitTestConfiguration.class})
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FindUserByIdInternalErrorIT {

  private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";
  private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtService jwtService;
  @Autowired private TokenService tokenService;

  @MockitoBean private FindUserByIdUseCase findUserByIdUseCase;

  @Test
  @DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
  void findUserById_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() throws Exception {
    long usersBeforeRequest = countUsers();
    when(findUserByIdUseCase.execute(OWNER_ID))
        .thenThrow(new RuntimeException("database stacktrace detail"));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/users/{id}",
            HttpMethod.GET,
            new HttpEntity<>(authenticatedHeaders()),
            String.class,
            OWNER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertThat(response.getBody(), not(containsString("database stacktrace detail")));
    assertThat(response.getBody(), not(containsString("RuntimeException")));
    assertThat(response.getBody(), not(containsString("at ")));
    assertThat(response.getBody(), not(containsString(".java:")));

    assertEquals(usersBeforeRequest, countUsers());
  }

  private HttpHeaders authenticatedHeaders() {
    UUID adminId = UUID.fromString(ADMIN_ID);
    HttpHeaders headers = new HttpHeaders();
    headers.add(
        HttpHeaders.COOKIE, "access_token=" + jwtService.generateToken(adminId, Set.of(UserRole.ADMIN)));
    headers.add(HttpHeaders.COOKIE, "refresh_token=" + tokenService.generateRefreshToken(adminId));
    return headers;
  }

  private long countUsers() {
    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
    return count == null ? 0 : count;
  }
}
