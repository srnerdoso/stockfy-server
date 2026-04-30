package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.application.usecase.UpdatePasswordUseCase;
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
import org.springframework.http.MediaType;
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
class UpdatePasswordInternalErrorIT {

  private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtService jwtService;
  @Autowired private TokenService tokenService;

  @MockitoBean private UpdatePasswordUseCase updatePasswordUseCase;

  @Test
  @DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
  void updatePassword_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() {
    String oldHash = passwordHash();
    doThrow(new RuntimeException("password update stacktrace detail"))
        .when(updatePasswordUseCase)
        .executeAuthenticated(OWNER_ID, "oldPassword123", "newPassword123", "newPassword123");

    HttpHeaders headers = authenticatedHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/users/password",
            HttpMethod.PATCH,
            new HttpEntity<>(
                "{\"currentPassword\":\"oldPassword123\","
                    + "\"newPassword\":\"newPassword123\","
                    + "\"confirmPassword\":\"newPassword123\"}",
                headers),
            String.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertThat(response.getBody(), not(containsString("password update stacktrace detail")));
    assertThat(response.getBody(), not(containsString("RuntimeException")));
    assertThat(response.getBody(), not(containsString("at ")));
    assertThat(response.getBody(), not(containsString(".java:")));
    assertEquals(oldHash, passwordHash());
  }

  private HttpHeaders authenticatedHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(
        HttpHeaders.COOKIE,
        "access_token=" + jwtService.generateToken(OWNER_ID, Set.of(UserRole.USER)));
    headers.add(HttpHeaders.COOKIE, "refresh_token=" + tokenService.generateRefreshToken(OWNER_ID));
    return headers;
  }

  private String passwordHash() {
    return jdbcTemplate.queryForObject(
        "SELECT password_hash FROM users WHERE id = ?::uuid", String.class, OWNER_ID);
  }
}
