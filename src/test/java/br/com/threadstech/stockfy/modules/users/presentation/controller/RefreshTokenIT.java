package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
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

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, RateLimitTestConfiguration.class})
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RefreshTokenIT {

  private static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private TokenService tokenService;
  @Autowired private StringRedisTemplate redisTemplate;
  @Autowired private UserRateLimitConfig rateLimitConfig;
  @Autowired private MutableTimeMeter rateLimitTimeMeter;

  @AfterEach
  void tearDown() {
    clearRateLimitBuckets();
    rateLimitTimeMeter.reset();
  }

  @Test
  @DisplayName("Deve retornar 200 com novos cookies e rotacionar refresh token")
  void refreshToken_whenRefreshTokenIsValid_thenReturns200WithNewCookiesAndRotatesToken()
      throws Exception {
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();
    String oldRefreshToken = tokenService.generateRefreshToken(USER_ID);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", oldRefreshToken)))
            .andExpect(status().isOk())
            .andExpect(content().string(""))
            .andExpect(cookie().exists("access_token"))
            .andExpect(cookie().exists("refresh_token"))
            .andExpect(cookie().httpOnly("access_token", true))
            .andExpect(cookie().httpOnly("refresh_token", true))
            .andReturn();

    Cookie newRefreshCookie = result.getResponse().getCookie("refresh_token");
    assertNotNull(newRefreshCookie);
    assertNotEquals(oldRefreshToken, newRefreshCookie.getValue());
    assertFalse(redisTemplate.hasKey("refresh_token:" + oldRefreshToken));
    assertTrue(redisTemplate.hasKey("refresh_token:" + newRefreshCookie.getValue()));
    assertThat(result.getResponse().getHeaders("Set-Cookie"), everyItem(containsString("Secure")));
    assertNoSensitiveData(result);
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  @Test
  @DisplayName("Deve retornar 401 sem corpo quando refresh token estiver ausente")
  void refreshToken_whenRefreshTokenIsMissing_thenReturns401WithoutBody() throws Exception {
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();

    MvcResult result =
        mockMvc
            .perform(post("/api/v1/auth/sessions/refresh"))
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

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", "invalid-refresh-token")))
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

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", "")))
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
    String revokedRefreshToken = tokenService.generateRefreshToken(USER_ID);
    tokenService.revokeRefreshToken(revokedRefreshToken);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", revokedRefreshToken)))
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
    String refreshToken = tokenService.generateRefreshToken(UUID.randomUUID());

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", refreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""))
            .andExpect(cookie().doesNotExist("access_token"))
            .andExpect(cookie().doesNotExist("refresh_token"))
            .andReturn();

    assertFalse(redisTemplate.hasKey("refresh_token:" + refreshToken));
    assertNoSensitiveData(result);
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  @Test
  @DisplayName("Deve retornar 401 sem corpo quando valor do Redis estiver malformado")
  void refreshToken_whenRedisValueIsMalformed_thenReturns401WithoutBody() throws Exception {
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();
    String refreshToken = "malformed-refresh-token";
    redisTemplate.opsForValue().set("refresh_token:" + refreshToken, "not-a-uuid");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", refreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""))
            .andExpect(cookie().doesNotExist("access_token"))
            .andExpect(cookie().doesNotExist("refresh_token"))
            .andReturn();

    assertFalse(redisTemplate.hasKey("refresh_token:" + refreshToken));
    assertNoSensitiveData(result);
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  @Test
  @DisplayName("Deve retornar 401 sem corpo quando usuario estiver bloqueado")
  void refreshToken_whenUserIsLocked_thenReturns401WithoutBody() throws Exception {
    jdbcTemplate.update("UPDATE users SET status = 'LOCKED' WHERE id = ?::uuid", USER_ID);
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();
    String refreshToken = tokenService.generateRefreshToken(USER_ID);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", refreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""))
            .andExpect(cookie().doesNotExist("access_token"))
            .andExpect(cookie().doesNotExist("refresh_token"))
            .andReturn();

    assertFalse(redisTemplate.hasKey("refresh_token:" + refreshToken));
    assertNoSensitiveData(result);
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  @Test
  @DisplayName("Deve retornar 401 sem corpo quando usuario estiver inativo")
  void refreshToken_whenUserIsInactive_thenReturns401WithoutBody() throws Exception {
    jdbcTemplate.update("UPDATE users SET active = FALSE WHERE id = ?::uuid", USER_ID);
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();
    String refreshToken = tokenService.generateRefreshToken(USER_ID);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/sessions/refresh")
                    .cookie(new Cookie("refresh_token", refreshToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""))
            .andExpect(cookie().doesNotExist("access_token"))
            .andExpect(cookie().doesNotExist("refresh_token"))
            .andReturn();

    assertFalse(redisTemplate.hasKey("refresh_token:" + refreshToken));
    assertNoSensitiveData(result);
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  @Test
  @DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
  void refreshToken_whenGeneralLimitExceeded_thenReturns429() throws Exception {
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();

    for (int i = 0; i < 10; i++) {
      mockMvc
          .perform(authenticatedRefreshRequest(tokenService.generateRefreshToken(USER_ID)))
          .andExpect(status().isOk());
    }

    String blockedRefreshToken = tokenService.generateRefreshToken(USER_ID);
    mockMvc
        .perform(authenticatedRefreshRequest(blockedRefreshToken))
        .andExpect(status().isTooManyRequests());

    assertTrue(redisTemplate.hasKey("refresh_token:" + blockedRefreshToken));
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  @Test
  @DisplayName("Deve permitir refresh quando a janela de rate limit expirar")
  void refreshToken_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
    long usersBeforeRequest = countUsers();
    UserSnapshot userBeforeRequest = userSnapshot();

    for (int i = 0; i < 10; i++) {
      mockMvc
          .perform(authenticatedRefreshRequest(tokenService.generateRefreshToken(USER_ID)))
          .andExpect(status().isOk());
    }

    mockMvc
        .perform(authenticatedRefreshRequest(tokenService.generateRefreshToken(USER_ID)))
        .andExpect(status().isTooManyRequests());

    rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

    MvcResult result =
        mockMvc
            .perform(authenticatedRefreshRequest(tokenService.generateRefreshToken(USER_ID)))
            .andExpect(status().isOk())
            .andExpect(content().string(""))
            .andReturn();

    assertNoSensitiveData(result);
    assertUserDataUnchanged(usersBeforeRequest, userBeforeRequest);
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
      authenticatedRefreshRequest(String refreshToken) {
    return post("/api/v1/auth/sessions/refresh")
        .cookie(new Cookie("refresh_token", refreshToken));
  }

  private void assertNoSensitiveData(MvcResult result) throws Exception {
    String body = result.getResponse().getContentAsString();
    assertThat(body, not(containsString("password")));
    assertThat(body, not(containsString("password_hash")));
    assertThat(body, not(containsString("resetCode")));
    assertThat(body, not(containsString("resetPasswordCodeHash")));
    assertThat(body, not(containsString("access_token")));
    assertThat(body, not(containsString("refresh_token")));
  }

  private long countUsers() {
    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
    return count == null ? 0 : count;
  }

  private UserSnapshot userSnapshot() {
    Map<String, Object> fields =
        jdbcTemplate.queryForMap(
            """
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
            """,
            USER_ID);
    List<String> roles =
        jdbcTemplate.queryForList(
            "SELECT role FROM users_roles WHERE user_id = ?::uuid ORDER BY role",
            String.class,
            USER_ID);
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
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private void clearBucketMap(String fieldName) throws ReflectiveOperationException {
    var field = UserRateLimitConfig.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    ((java.util.Map<?, ?>) field.get(rateLimitConfig)).clear();
  }

  private record UserSnapshot(Map<String, Object> fields, List<String> roles) {}
}
