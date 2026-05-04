package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtAuthenticationFilter;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import jakarta.servlet.http.Cookie;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

  private final TokenService tokenService = org.mockito.Mockito.mock(TokenService.class);
  private final JwtService jwtService = new JwtService();
  private final JwtAuthenticationFilter filter =
      new JwtAuthenticationFilter(jwtService, tokenService);

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Deve criar uma authority para cada role presente no token")
  void doFilterInternal_whenTokenHasMultipleRoles_thenCreatesAuthorityForEachRole()
      throws Exception {
    UUID userId = UUID.randomUUID();
    ReflectionTestUtils.setField(
        jwtService,
        "secret",
        "9a4f2c8d3b7a1e5f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1d");
    ReflectionTestUtils.setField(jwtService, "expiration", 900000L);
    String accessToken = jwtService.generateToken(userId, Set.of(UserRole.ADMIN, UserRole.USER));
    String refreshToken = "refresh-token";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("access_token", accessToken), new Cookie("refresh_token", refreshToken));

    when(tokenService.getUserIdFromRefreshToken(refreshToken)).thenReturn(userId);
    when(tokenService.validateRefreshToken(refreshToken)).thenReturn(true);

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
  }
}
