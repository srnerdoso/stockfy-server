package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.threadstech.stockfy.modules.users.infrastructure.config.UserRateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  private static final String CLIENT_IP = "127.0.0.1";

  @Mock private UserRateLimitConfig rateLimitConfig;
  @Mock private Bucket loginBucket;
  @Mock private Bucket generalBucket;
  @Mock private FilterChain filterChain;

  @Test
  @DisplayName("Deve usar bucket de login apenas para criacao de sessao")
  void doFilter_whenPostLoginEndpoint_thenUsesLoginBucket() throws Exception {
    when(rateLimitConfig.resolveLoginBucket(CLIENT_IP)).thenReturn(loginBucket);
    when(loginBucket.tryConsume(1)).thenReturn(true);

    new RateLimitFilter(rateLimitConfig)
        .doFilter(loginRequest(), new MockHttpServletResponse(), filterChain);

    verify(rateLimitConfig).resolveLoginBucket(CLIENT_IP);
    verify(rateLimitConfig, never()).resolveGeneralBucket(anyString());
  }

  @Test
  @DisplayName("Deve usar bucket geral para renovacao de sessao")
  void doFilter_whenPostRefreshEndpoint_thenUsesGeneralBucket() throws Exception {
    when(rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).thenReturn(generalBucket);
    when(generalBucket.tryConsume(1)).thenReturn(true);

    new RateLimitFilter(rateLimitConfig)
        .doFilter(refreshRequest(), new MockHttpServletResponse(), filterChain);

    verify(rateLimitConfig).resolveGeneralBucket(CLIENT_IP);
    verify(rateLimitConfig, never()).resolveLoginBucket(anyString());
  }

  @Test
  @DisplayName("Deve usar bucket geral para encerramento de sessao")
  void doFilter_whenDeleteCurrentSessionEndpoint_thenUsesGeneralBucket() throws Exception {
    when(rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).thenReturn(generalBucket);
    when(generalBucket.tryConsume(1)).thenReturn(true);

    new RateLimitFilter(rateLimitConfig)
        .doFilter(logoutRequest(), new MockHttpServletResponse(), filterChain);

    verify(rateLimitConfig).resolveGeneralBucket(CLIENT_IP);
    verify(rateLimitConfig, never()).resolveLoginBucket(anyString());
  }

  @Test
  @DisplayName("Deve retornar 429 sem corpo para encerramento de sessao")
  void doFilter_whenLogoutLimitExceeded_thenReturns429WithoutBody() throws Exception {
    when(rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).thenReturn(generalBucket);
    when(generalBucket.tryConsume(1)).thenReturn(false);

    var response = new MockHttpServletResponse();
    new RateLimitFilter(rateLimitConfig).doFilter(logoutRequest(), response, filterChain);

    assertEquals(429, response.getStatus());
    assertEquals("", response.getContentAsString());
  }

  @Test
  @DisplayName("Deve preservar corpo de rate limit para demais endpoints")
  void doFilter_whenNonLogoutLimitExceeded_thenReturns429WithMessage() throws Exception {
    when(rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).thenReturn(generalBucket);
    when(generalBucket.tryConsume(1)).thenReturn(false);

    var response = new MockHttpServletResponse();
    new RateLimitFilter(rateLimitConfig).doFilter(refreshRequest(), response, filterChain);

    assertEquals(429, response.getStatus());
    assertEquals("Too many requests", response.getContentAsString());
  }

  private MockHttpServletRequest loginRequest() {
    var request = new MockHttpServletRequest("POST", "/api/v1/auth/sessions");
    request.setRemoteAddr(CLIENT_IP);
    return request;
  }

  private MockHttpServletRequest refreshRequest() {
    var request = new MockHttpServletRequest("POST", "/api/v1/auth/sessions/refresh");
    request.setRemoteAddr(CLIENT_IP);
    return request;
  }

  private MockHttpServletRequest logoutRequest() {
    var request = new MockHttpServletRequest("DELETE", "/api/v1/auth/sessions/current");
    request.setRemoteAddr(CLIENT_IP);
    return request;
  }
}
