package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.modules.users.application.dto.LoginRequest;
import br.com.threadstech.stockfy.modules.users.application.usecase.LoginUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LoginUseCase loginUseCase;

  @Test
  @DisplayName("Should return cookies on login")
  void shouldReturnCookiesOnLogin() throws Exception {
    when(loginUseCase.execute(anyString(), anyString()))
        .thenReturn(new AuthResponse("access", "refresh"));

    var request = new LoginRequest("test@example.com", "password");

    mockMvc
        .perform(
            post("/api/v1/auth/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("access_token"))
        .andExpect(cookie().exists("refresh_token"))
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(cookie().httpOnly("refresh_token", true));
  }
}
