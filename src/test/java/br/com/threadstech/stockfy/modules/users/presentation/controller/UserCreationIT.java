package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
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
    String json =
        """
            {
                "name": "John Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "ADMIN"
            }
            """;

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isCreated());

    assert (userRepository
        .findByEmail(
            new br.com.threadstech.stockfy.modules.users.domain.model.Email("john@example.com"))
        .isPresent());
  }

  @Test
  @DisplayName("Deve retornar 401 quando não houver token de autenticação")
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
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Deve retornar 403 quando o usuário autenticado for um USER")
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
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve retornar 400 quando o nome for nulo ou vazio")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenNameIsInvalid_thenReturns400() throws Exception {
    String jsonNull = """
        { "email": "john@example.com", "password": "password123", "role": "USER" }
        """;
    String jsonBlank = """
        { "name": " ", "email": "john@example.com", "password": "password123", "role": "USER" }
        """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonBlank))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve retornar 400 quando o email for nulo ou inválido")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenEmailIsInvalid_thenReturns400() throws Exception {
    String jsonNull = """
        { "name": "John", "password": "password123", "role": "USER" }
        """;
    String jsonInvalid = """
        { "name": "John", "email": "invalid-email", "password": "password123", "role": "USER" }
        """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve retornar 400 quando a senha for nula ou em branco")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenPasswordIsInvalid_thenReturns400() throws Exception {
    String jsonNull = """
        { "name": "John", "email": "john@example.com", "role": "USER" }
        """;
    String jsonBlank = """
        { "name": "John", "email": "john@example.com", "password": " ", "role": "USER" }
        """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonBlank))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve retornar 400 quando a role for nula ou inválida")
  @WithMockUser(roles = "ADMIN")
  void registerUser_whenRoleIsMissingOrInvalid_thenReturns400() throws Exception {
    String jsonMissing = """
        { "name": "John", "email": "john@example.com", "password": "password123" }
        """;
    String jsonInvalid = """
        { "name": "John", "email": "john@example.com", "password": "password123", "role": "INVALID" }
        """;

    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonMissing))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid))
        .andExpect(status().isBadRequest());
  }


  @Test
  @DisplayName("Deve retornar 422 quando as senhas não coincidem")
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

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isUnprocessableEntity());
  }

  // FIXME: Trocar isxx por bad request ou unprocessable entity após verificar qual o status real
  // retornado
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

    // Excede o limite de 10
    for (int i = 0; i < 11; i++) {
      var result =
          mockMvc.perform(
              post("/api/v1/users")
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
    String json =
        """
            {
                "name": "User",
                "email": "user@test.com",
                "password": "password123",
                "role": "USER"
            }
            """;

    // Excede
    for (int i = 0; i < 11; i++) {
      mockMvc.perform(
          post("/api/v1/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(json.replace("user@test.com", "user" + i + "@test.com")));
    }

    // Simula passagem de tempo (1 minuto + margem)
    Thread.sleep(61000);

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.replace("user@test.com", "after-window@test.com")))
        .andExpect(status().isCreated());
  }
}
