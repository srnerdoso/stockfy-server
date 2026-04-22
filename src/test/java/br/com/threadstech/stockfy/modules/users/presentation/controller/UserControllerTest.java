package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserRole;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserRepository userRepository;

  private final UUID userId = UUID.randomUUID();

  @Test
  @DisplayName("Should return user profile when authenticated")
  void shouldReturnUserProfile() throws Exception {
    User user =
        User.builder()
            .id(userId)
            .name("John Doe")
            .email(new Email("john@example.com"))
            .role(UserRole.USER)
            .status(UserStatus.ACTIVE)
            .active(true)
            .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Manually set security context for test
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

    mockMvc
        .perform(get("/api/v1/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }
}
