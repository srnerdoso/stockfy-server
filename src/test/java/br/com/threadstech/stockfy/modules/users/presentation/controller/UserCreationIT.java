package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserCreationIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("Deve retornar 201 e criar usuário quando os dados forem válidos")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenDataIsValid_thenReturns201AndCreatesUser() throws Exception {
    String json = """
            {
                "name": "John Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "ADMIN"
            }
            """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated())
        .andExpect(content().string(""));

    assertTrue(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Deve retornar 401 quando não houver token de autenticação")
  void registerUser_whenNotAuthenticated_thenReturns401() throws Exception {
    String json = """
            {
                "name": "John Doe",
                "email": "unauth@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Deve retornar 403 quando o usuário autenticado for um USER")
  @WithMockUser(roles = "USER")
  void registerUser_whenUserRole_thenReturns403() throws Exception {
    String json = """
            {
                "name": "John Doe",
                "email": "user@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando o nome for inválido")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenNameIsInvalid_thenReturns400() throws Exception {
    String jsonNull = "{ \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"USER\" }";

    var result = mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull));
    assertBadRequestError(result, "name", "must not be blank");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando o email for inválido")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenEmailIsInvalid_thenReturns400() throws Exception {
    String jsonNull = "{ \"name\": \"John\", \"password\": \"password123\", \"role\": \"USER\" }";
    String jsonInvalid = "{ \"name\": \"John\", \"email\": \"invalid-email\", \"password\": \"password123\", \"role\": \"USER\" }";

    assertBadRequestError(mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull)), "email", "must not be blank");
    assertBadRequestError(mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)), "email", "must be a well-formed email address");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando a senha for inválida")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenPasswordIsInvalid_thenReturns400() throws Exception {
    String jsonNull = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"role\": \"USER\" }";
    String jsonBlank = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \" \", \"role\": \"USER\" }";

    assertBadRequestError(mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull)), "password", "must not be blank");
    assertBadRequestError(mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonBlank)), "password", "must not be blank");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Não deve persistir usuário e deve retornar 400 quando a role for inválida")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenRoleIsMissingOrInvalid_thenReturns400() throws Exception {
    String jsonMissing = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \"password123\" }";
    String jsonInvalid = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"INVALID\" }";

    assertBadRequestError(mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonMissing)), "role", "must not be null");
    assertBadRequestError(mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)), "role", "must not be null");
    assertFalse(userRepository.findByEmail(new Email("john@example.com")).isPresent());
  }

  @Test
  @DisplayName("Deve retornar 422 quando as senhas não coincidem")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenPasswordsDoNotMatch_thenReturns422() throws Exception {
    String json = """
            {
                "name": "John Doe",
                "email": "test@example.com",
                "password": "password123",
                "confirmPassword": "differentPassword",
                "role": "USER"
            }
            """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @DisplayName("Deve retornar 400 ou 422 para SQL Injection (tratado como validação inválida)")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenInputContainsSqlInjection_thenReturnsBadRequest() throws Exception {
    String json = """
            {
                "name": "User'); DROP TABLE users; --",
                "email": "sql@example.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Deve retornar 429 quando exceder limite de 10 requisições por minuto")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenLimitExceeded_thenReturns429() throws Exception {
    String json = """
            {
                "name": "User",
                "email": "user@test.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    for (int i = 0; i < 11; i++) {
      var result = mockMvc.perform(post("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json.replace("user@test.com", "user" + i + "@test.com")));

      if (i < 10) {
        result.andExpect(status().isCreated());
      } else {
        result.andExpect(status().isTooManyRequests());
      }
    }
  }

  @Test
  @DisplayName("Deve permitir requisições após o período de janela de 1 minuto")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenWindowPasses_thenReturns201() throws Exception {
    String json = """
            {
                "name": "User",
                "email": "user@test.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    for (int i = 0; i < 11; i++) {
      mockMvc.perform(post("/api/v1/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json.replace("user@test.com", "user" + i + "@test.com")));
    }

    Thread.sleep(61000);

    mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.replace("user@test.com", "after-window@test.com")))
        .andExpect(status().isCreated());
  }

  private void assertBadRequestError(ResultActions result, String field, String message) throws Exception {
    result.andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.type").value("about:blank"))
          .andExpect(jsonPath("$.title").value("Validation Error"))
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.detail").value("Erro de validação nos campos informados."))
          .andExpect(jsonPath("$.fieldErrors[0].field").value(field))
          .andExpect(jsonPath("$.fieldErrors[0].message").value(message));
  }
}
