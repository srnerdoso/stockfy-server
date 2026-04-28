package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtService;
import io.github.bucket4j.TimeMeter;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, UserCreationIT.RateLimitTimeTestConfiguration.class})
class UserCreationIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private UserRateLimitConfig rateLimitConfig;
  @Autowired private JwtService jwtService;
  @Autowired private MutableTimeMeter rateLimitTimeMeter;

  @AfterEach
  void tearDown() {
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
  @DisplayName("Deve retornar 201 e criar usuário quando os dados forem válidos")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenDataIsValid_thenReturns201AndCreatesUser() throws Exception {
    String json =
        """
            {
                "name": "John Doe",
                "email": "created@example.com",
                "password": "password123",
                "role": "ADMIN"
            }
            """;

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(content().string(""));

    assertTrue(userRepository.findByEmail(new Email("created@example.com")).isPresent());
  }

  @Test
  @DisplayName("Deve retornar 201 e criar usuário quando token JWT pertencer a ADMIN")
  void registerUser_whenJwtTokenBelongsToAdmin_thenReturns201AndCreatesUser() throws Exception {
    User admin = createUser("Jwt Admin", "jwt-admin@example.com", UserRole.ADMIN);
    userRepository.save(admin);
    String accessToken = jwtService.generateToken(admin.getId(), admin.getRole().name());
    String json =
        """
            {
                "name": "Jwt Created",
                "email": "jwt-created@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc
        .perform(
            post("/api/v1/users")
                .cookie(new Cookie("access_token", accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isCreated())
        .andExpect(content().string(""));

    assertTrue(userRepository.findByEmail(new Email("jwt-created@example.com")).isPresent());
  }

  @Test
  @DisplayName(
      "Não deve persistir usuário e deve retornar 401 quando não houver token de autenticação")
  void registerUser_whenNotAuthenticated_thenReturns401() throws Exception {
    String json =
        """
            {
                "name": "John Doe",
                "email": "unauth@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(""))
        .andExpect(result -> assertFalse(result.getResponse().isCommitted()));

    assertFalse(userRepository.findByEmail(new Email("unauth@example.com")).isPresent());
  }

  @Test
  @DisplayName(
      "Não deve persistir usuário e deve retornar 403 quando o usuário autenticado for um USER")
  @WithMockUser(roles = "USER")
  void registerUser_whenUserRole_thenReturns403() throws Exception {
    String json =
        """
            {
                "name": "John Doe",
                "email": "user@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isForbidden())
        .andExpect(content().string(""))
        .andExpect(result -> assertFalse(result.getResponse().isCommitted()));

    assertFalse(userRepository.findByEmail(new Email("user@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando o nome for inválido")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenNameIsInvalid_thenReturns400() throws Exception {
    String jsonNull =
        "{ \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"USER\" }";

    var result =
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull));
    assertBadRequestError(result, "name", "O nome é obrigatório.");

    String jsonInvalid =
        "{ \"name\": \"User'); DROP TABLE users; --\", \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"USER\" }";

    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)),
        "name",
        "O nome contém caracteres inválidos.");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando o email for inválido")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenEmailIsInvalid_thenReturns400() throws Exception {
    String jsonNull = "{ \"name\": \"John\", \"password\": \"password123\", \"role\": \"USER\" }";
    String jsonInvalid =
        "{ \"name\": \"John\", \"email\": \"invalid-email\", \"password\": \"password123\", \"role\": \"USER\" }";

    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull)),
        "email",
        "O e-mail é obrigatório.");
    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)),
        "email",
        "O e-mail informado é inválido.");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando a senha for inválida")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenPasswordIsInvalid_thenReturns400() throws Exception {
    String jsonNull = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"role\": \"USER\" }";
    String jsonBlank =
        "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \" \", \"role\": \"USER\" }";

    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull)),
        "password",
        "A senha é obrigatória.");
    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonBlank)),
        "password",
        "A senha é obrigatória.");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando a role for inválida")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenRoleIsMissingOrInvalid_thenReturns400() throws Exception {
    String jsonMissing =
        "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \"password123\" }";
    String jsonInvalid =
        "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"INVALID\" }";

    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonMissing)),
        "role",
        "O perfil do usuário é obrigatório.");
    assertBadRequestError(
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)),
        "role",
        "não deve ser nulo");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 422 quando as senhas não coincidem")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenPasswordsDoNotMatch_thenReturns422() throws Exception {
    String json =
        """
            {
                "name": "John Doe",
                "email": "test@example.com",
                "password": "password123",
                "confirmPassword": "differentPassword",
                "role": "USER"
            }
            """;

    var result =
        mockMvc.perform(
            post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json));

    result
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.detail").value("As senhas não coincidem."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("As senhas não coincidem."));

    assertFalse(userRepository.findByEmail(new Email("test@example.com")).isPresent());
  }

  @Test
  @DisplayName("Deve retornar 400 ou 422 para SQL Injection (tratado como validação inválida)")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenInputContainsSqlInjection_thenReturnsBadRequest() throws Exception {
    String json =
        """
            {
                "name": "User'); DROP TABLE users; --",
                "email": "sql@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Deve retornar 429 quando exceder limite de 10 requisições por minuto")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenLimitExceeded_thenReturns429() throws Exception {
    String json =
        """
            {
                "name": "User",
                "email": "user@test.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    for (int i = 0; i <= 11; i++) {
      var result =
          mockMvc.perform(
              post("/api/v1/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(json.replace("user@test.com", "user" + i + "@test.com")));

      if (i > 10) {
        result.andExpect(status().isTooManyRequests());
      }
    }
  }

  @Test
  @DisplayName("Deve permitir requisições após o período de janela de 1 minuto")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenWindowPasses_thenReturns201() throws Exception {
    String json =
        """
            {
                "name": "User",
                "email": "user@test.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    for (int i = 0; i < 11; i++) {
      mockMvc.perform(
          post("/api/v1/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(json.replace("user@test.com", "user" + i + "@test.com")));
    }

    rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.replace("user@test.com", "after-window@test.com")))
        .andExpect(status().isCreated());
  }

  private void assertBadRequestError(ResultActions result, String field, String message)
      throws Exception {
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Validation Error"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Erro de validação nos campos informados."))
        .andExpect(jsonPath("$.fieldErrors[0].field").value(field))
        .andExpect(jsonPath("$.fieldErrors[0].message").value(message));
  }

  private User createUser(String name, String email, UserRole role) {
    return User.builder()
        .id(UUID.randomUUID())
        .name(name)
        .email(new Email(email))
        .password(new Password("password123"))
        .role(role)
        .status(UserStatus.ACTIVE)
        .active(true)
        .build();
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class RateLimitTimeTestConfiguration {

    @Bean
    @Primary
    MutableTimeMeter mutableRateLimitTimeMeter() {
      return new MutableTimeMeter();
    }
  }

  static class MutableTimeMeter implements TimeMeter {

    private final AtomicLong currentTimeNanos = new AtomicLong();

    @Override
    public long currentTimeNanos() {
      return currentTimeNanos.get();
    }

    @Override
    public boolean isWallClockBased() {
      return false;
    }

    void advanceBy(Duration duration) {
      currentTimeNanos.addAndGet(duration.toNanos());
    }

    void reset() {
      currentTimeNanos.set(0);
    }
  }
}
