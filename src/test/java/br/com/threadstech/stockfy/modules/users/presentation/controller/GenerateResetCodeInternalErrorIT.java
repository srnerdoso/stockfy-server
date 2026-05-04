package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.usecase.GenerateResetCodeUseCase;
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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "server.error.include-stacktrace=never")
@Import({TestcontainersConfiguration.class, RateLimitTestConfiguration.class})
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class GenerateResetCodeInternalErrorIT {

  private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtService jwtService;
  @Autowired private TokenService tokenService;

  @MockitoBean private GenerateResetCodeUseCase generateResetCodeUseCase;

  @Test
  @DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
  void generateResetCode_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() {
    String oldHash = resetPasswordCodeHash();
    when(generateResetCodeUseCase.execute(OWNER_ID))
        .thenThrow(new RuntimeException("reset code stacktrace detail"));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/users/{id}/password-reset-codes",
            HttpMethod.POST,
            new HttpEntity<>(authenticatedHeaders()),
            String.class,
            OWNER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertThat(response.getBody(), not(containsString("reset code stacktrace detail")));
    assertThat(response.getBody(), not(containsString("RuntimeException")));
    assertThat(response.getBody(), not(containsString("at ")));
    assertThat(response.getBody(), not(containsString(".java:")));
    assertEquals(oldHash, resetPasswordCodeHash());
  }

  private HttpHeaders authenticatedHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(
        HttpHeaders.COOKIE,
        "access_token=" + jwtService.generateToken(ADMIN_ID, Set.of(UserRole.ADMIN)));
    headers.add(HttpHeaders.COOKIE, "refresh_token=" + tokenService.generateRefreshToken(ADMIN_ID));
    return headers;
  }

  private String resetPasswordCodeHash() {
    return jdbcTemplate.queryForObject(
        "SELECT reset_password_code_hash FROM users WHERE id = ?::uuid", String.class, OWNER_ID);
  }
}
