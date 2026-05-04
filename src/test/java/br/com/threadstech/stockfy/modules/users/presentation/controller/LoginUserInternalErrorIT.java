package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.usecase.LoginUseCase;
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
class LoginUserInternalErrorIT {

  private static final String USER_ID = "00000000-0000-0000-0000-000000000002";

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;

  @MockitoBean private LoginUseCase loginUseCase;

  @Test
  @DisplayName("Deve retornar 500 sem stacktrace quando ocorrer erro interno")
  void loginUser_whenUnexpectedErrorOccurs_thenReturns500WithoutStacktrace() {
    String oldStatus = userStatus();
    doThrow(new RuntimeException("login database stacktrace detail"))
        .when(loginUseCase)
        .execute("bruno.user@example.com", "password123");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/auth/sessions",
            HttpMethod.POST,
            new HttpEntity<>(
                "{\"email\":\"bruno.user@example.com\",\"password\":\"password123\"}", headers()),
            String.class);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertThat(response.getBody(), not(containsString("login database stacktrace detail")));
    assertThat(response.getBody(), not(containsString("RuntimeException")));
    assertThat(response.getBody(), not(containsString("at ")));
    assertThat(response.getBody(), not(containsString(".java:")));
    assertThat(response.getHeaders().toString(), not(containsString("access_token")));
    assertThat(response.getHeaders().toString(), not(containsString("refresh_token")));
    assertEquals(oldStatus, userStatus());
  }

  private HttpHeaders headers() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private String userStatus() {
    return jdbcTemplate.queryForObject(
        "SELECT status FROM users WHERE id = ?::uuid", String.class, USER_ID);
  }
}
