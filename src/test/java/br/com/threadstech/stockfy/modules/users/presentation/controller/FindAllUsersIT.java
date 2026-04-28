package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.infrastructure.config.UserRateLimitConfig;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, RateLimitTestConfiguration.class})
@Sql(
    scripts = "/sql/users/find-all/cleanup.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = "/sql/users/find-all/base-users.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = "/sql/users/find-all/cleanup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FindAllUsersIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private UserRateLimitConfig rateLimitConfig;
  @Autowired private MutableTimeMeter rateLimitTimeMeter;

  @AfterEach
  void tearDownRateLimit() {
    try {
      var loginBucketsField = UserRateLimitConfig.class.getDeclaredField("loginBuckets");
      loginBucketsField.setAccessible(true);
      ((java.util.Map<?, ?>) loginBucketsField.get(rateLimitConfig)).clear();

      var generalBucketsField = UserRateLimitConfig.class.getDeclaredField("generalBuckets");
      generalBucketsField.setAccessible(true);
      ((java.util.Map<?, ?>) generalBucketsField.get(rateLimitConfig)).clear();
      rateLimitTimeMeter.reset();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("Deve retornar pagina SUMMARY apenas com nome, email e role")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenTypeSummary_thenReturnsOnlySummaryFields() throws Exception {
    long usersBeforeRequest = countUsers();

    mockMvc
        .perform(
            get("/api/v1/users").param("type", "SUMMARY").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(4))
        .andExpect(jsonPath("$.content[0].name").exists())
        .andExpect(jsonPath("$.content[0].email").exists())
        .andExpect(jsonPath("$.content[0].role").exists())
        .andExpect(jsonPath("$.content[0].id").doesNotExist())
        .andExpect(jsonPath("$.content[0].status").doesNotExist())
        .andExpect(jsonPath("$.content[0].active").doesNotExist())
        .andExpect(jsonPath("$.content[0].createdAt").doesNotExist())
        .andExpect(jsonPath("$.content[0].createdBy").doesNotExist())
        .andExpect(jsonPath("$.content[0].updatedAt").doesNotExist())
        .andExpect(jsonPath("$.content[0].updatedBy").doesNotExist())
        .andExpect(jsonPath("$.content[0].password").doesNotExist())
        .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
        .andExpect(jsonPath("$.content[0].resetCode").doesNotExist())
        .andExpect(jsonPath("$.content[0].resetPasswordCodeHash").doesNotExist())
        .andExpect(jsonPath("$.content[0].resetPasswordExpiresAt").doesNotExist())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.totalElements").value(4));

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
    assertTrue(userExists("bruno.user@example.com"));
  }

  @Test
  @DisplayName("Deve retornar pagina DETAILED com status e auditoria")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenTypeDetailed_thenReturnsDetailedFields() throws Exception {
    long usersBeforeRequest = countUsers();

    mockMvc
        .perform(
            get("/api/v1/users").param("type", "DETAILED").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").exists())
        .andExpect(jsonPath("$.content[0].email").exists())
        .andExpect(jsonPath("$.content[0].role").exists())
        .andExpect(jsonPath("$.content[0].status").exists())
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].createdBy").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists())
        .andExpect(jsonPath("$.content[0].updatedBy").exists())
        .andExpect(jsonPath("$.content[0].id").doesNotExist())
        .andExpect(jsonPath("$.content[0].active").doesNotExist())
        .andExpect(jsonPath("$.content[0].password").doesNotExist())
        .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
        .andExpect(jsonPath("$.content[0].resetCode").doesNotExist())
        .andExpect(jsonPath("$.content[0].resetPasswordCodeHash").doesNotExist())
        .andExpect(jsonPath("$.content[0].resetPasswordExpiresAt").doesNotExist());

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
  }

  @Test
  @DisplayName("Deve filtrar usuarios por nome parcial")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenNameFilterIsProvided_thenReturnsMatchingUsers() throws Exception {
    long usersBeforeRequest = countUsers();

    mockMvc
        .perform(get("/api/v1/users").param("type", "SUMMARY").param("name", "Ali"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Alice Filter"));

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("alice.filter@example.com"));
    assertTrue(userExists("bob.filter@example.com"));
  }

  @Test
  @DisplayName("Deve respeitar os limites de paginacao")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenPageSizeIsOne_thenReturnsOneItemAndTotal() throws Exception {
    long usersBeforeRequest = countUsers();

    mockMvc
        .perform(
            get("/api/v1/users").param("type", "SUMMARY").param("page", "0").param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.totalPages").value(4));

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
    assertTrue(userExists("bruno.user@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 400 quando type for omitido")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenTypeIsMissing_thenReturns400() throws Exception {
    long usersBeforeRequest = countUsers();

    assertValidationError(
        mockMvc.perform(get("/api/v1/users")), "type", "O tipo de listagem e obrigatorio.");

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 400 quando type for invalido")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenTypeIsInvalid_thenReturns400() throws Exception {
    long usersBeforeRequest = countUsers();

    assertValidationError(
        mockMvc.perform(get("/api/v1/users").param("type", "FULL")),
        "type",
        "O tipo de listagem deve ser SUMMARY ou DETAILED.");

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("bruno.user@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 400 quando name exceder o limite da coluna")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenNameExceedsColumnLimit_thenReturns400() throws Exception {
    String longName = "a".repeat(256);
    long usersBeforeRequest = countUsers();

    assertValidationError(
        mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY").param("name", longName)),
        "name",
        "O nome deve ter no maximo 255 caracteres.");

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("alice.filter@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 401 quando nao autenticado")
  void findAllUsers_whenNotAuthenticated_thenReturns401() throws Exception {
    long usersBeforeRequest = countUsers();

    mockMvc
        .perform(get("/api/v1/users").param("type", "SUMMARY"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(""));

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 403 quando usuario nao for ADMIN")
  @WithMockUser(roles = "USER")
  void findAllUsers_whenUserIsNotAdmin_thenReturns403() throws Exception {
    long usersBeforeRequest = countUsers();

    mockMvc
        .perform(get("/api/v1/users").param("type", "SUMMARY"))
        .andExpect(status().isForbidden())
        .andExpect(content().string(""));

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("bruno.user@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 400 quando name contiver SQL Injection")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenNameContainsSqlInjection_thenReturns400() throws Exception {
    long usersBeforeRequest = countUsers();

    assertValidationError(
        mockMvc.perform(
            get("/api/v1/users")
                .param("type", "SUMMARY")
                .param("name", "x%' OR 1=1; DROP TABLE users; --")),
        "name",
        "O nome contém caracteres inválidos.");

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
    assertTrue(userExists("alice.filter@example.com"));
  }

  @Test
  @DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenLimitExceeded_thenReturns429() throws Exception {
    long usersBeforeRequest = countUsers();

    for (int i = 0; i <= 11; i++) {
      var result = mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY"));
      if (i > 10) {
        result.andExpect(status().isTooManyRequests());
      }
    }

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
  }

  @Test
  @DisplayName("Deve permitir nova requisicao quando a janela de rate limit expirar")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_whenRateLimitWindowExpires_thenReturns200() throws Exception {
    long usersBeforeRequest = countUsers();

    for (int i = 0; i < 10; i++) {
      mockMvc.perform(get("/api/v1/users").param("type", "SUMMARY")).andExpect(status().isOk());
    }

    rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

    mockMvc
        .perform(get("/api/v1/users").param("type", "SUMMARY"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(4));

    assertEquals(usersBeforeRequest, countUsers());
    assertTrue(userExists("ana.admin@example.com"));
  }

  private long countUsers() {
    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
    return count == null ? 0 : count;
  }

  private boolean userExists(String email) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
    return count != null && count == 1;
  }

  private void assertValidationError(ResultActions result, String field, String message)
      throws Exception {
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Validation Error"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Erro de validação nos campos informados."))
        .andExpect(jsonPath("$.instance").doesNotExist())
        .andExpect(jsonPath("$.fieldErrors.length()").value(1))
        .andExpect(jsonPath("$.fieldErrors[0].field").value(field))
        .andExpect(jsonPath("$.fieldErrors[0].message").value(message));
  }
}
