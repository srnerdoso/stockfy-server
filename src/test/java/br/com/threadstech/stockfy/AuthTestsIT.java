package br.com.threadstech.stockfy;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.threadstech.stockfy.annotations.IntegrationTests;
import br.com.threadstech.stockfy.components.CookieUtils;
import br.com.threadstech.stockfy.security.jwt.JwtUserDetailsService;
import br.com.threadstech.stockfy.security.jwt.JwtUtils;
import br.com.threadstech.stockfy.security.refreshtoken.RefreshToken;
import br.com.threadstech.stockfy.security.refreshtoken.RefreshTokenRepository;
import br.com.threadstech.stockfy.utils.AuthTestUtils;
import jakarta.servlet.http.Cookie;
import java.util.Collection;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Slf4j
@IntegrationTests
@Sql(
    scripts = {
      "/sql/employee-contacts-insert.sql",
      "/sql/employee-addresses-insert.sql",
      "/sql/employees-insert.sql"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/employees-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthTestsIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private CookieUtils cookieUtils;
  @Autowired private JwtUtils jwtUtils;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private JwtUserDetailsService detailsService;

  @Test
  void shouldLoginWithReturnStatusNoContent() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post(AuthTestUtils.loginPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(AuthTestUtils.validLoginJson()))
            .andExpect(status().isNoContent())
            .andExpect(cookie().exists(cookieUtils.getAccessTokenCookieName()))
            .andExpect(cookie().exists(cookieUtils.getRefreshTokenCookieName()))
            .andExpect(cookie().httpOnly(cookieUtils.getAccessTokenCookieName(), true))
            .andExpect(cookie().httpOnly(cookieUtils.getRefreshTokenCookieName(), true))
            .andExpect(cookie().path(cookieUtils.getAccessTokenCookieName(), "/"))
            .andExpect(cookie().path(cookieUtils.getRefreshTokenCookieName(), "/"))
            .andReturn();

    Cookie[] cookies = result.getResponse().getCookies();
    String accessToken =
        cookieUtils.getCookieByName(cookies, cookieUtils.getAccessTokenCookieName()).getValue();
    String refreshTokenStr =
        cookieUtils.getCookieByName(cookies, cookieUtils.getRefreshTokenCookieName()).getValue();
    RefreshToken refreshToken =
        refreshTokenRepository.findByToken(UUID.fromString(refreshTokenStr)).orElse(null);

    String username = jwtUtils.getUsernameFromToken(accessToken);

    assertThat(accessToken).isNotBlank();
    assertThat(jwtUtils.isTokenValid(accessToken)).isTrue();
    assertThat(jwtUtils.getIdFromToken(accessToken)).isEqualTo(AuthTestUtils.employeeId);
    assertThat(username).isEqualTo(AuthTestUtils.email100);

    assertThat(refreshTokenStr).isNotBlank();
    assertThat(refreshToken).isNotNull();
    assertThat(refreshToken.getEmployee().getId()).isEqualTo(AuthTestUtils.employeeId);

    UserDetails userDetails = detailsService.loadUserByUsername(username);
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertThat(authorities).isNotEmpty();
    assertThat(authorities.size()).isEqualTo(1);
    assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
  }

  @Test
  void shouldLoginWithReturnStatusBadRequest() throws Exception {
    mockMvc
        .perform(
            post(AuthTestUtils.loginPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthTestUtils.invalidLoginJson()))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post(AuthTestUtils.loginPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthTestUtils.blankFieldsLoginJson()))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post(AuthTestUtils.loginPath())
                .contentType(MediaType.APPLICATION_JSON)
                .content(AuthTestUtils.nullFieldsLoginJson()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldLogoutWithReturnStatusNoContent() throws Exception {
    LoginCookies loginCookies = getLoginCookies();
    Cookie accessToken = loginCookies.accessToken();
    Cookie refreshToken = loginCookies.refreshToken();

    mockMvc
        .perform(delete(AuthTestUtils.logoutPath()).cookie(accessToken, refreshToken))
        .andExpect(status().isNoContent())
        .andExpect(cookie().maxAge(cookieUtils.getAccessTokenCookieName(), 0))
        .andExpect(cookie().maxAge(cookieUtils.getRefreshTokenCookieName(), 0))
        .andExpect(cookie().path(cookieUtils.getAccessTokenCookieName(), "/"))
        .andExpect(cookie().path(cookieUtils.getRefreshTokenCookieName(), "/"))
        .andExpect(cookie().httpOnly(cookieUtils.getAccessTokenCookieName(), true))
        .andExpect(cookie().httpOnly(cookieUtils.getRefreshTokenCookieName(), true))
        .andExpect(cookie().value(cookieUtils.getAccessTokenCookieName(), ""))
        .andExpect(cookie().value(cookieUtils.getRefreshTokenCookieName(), ""));

    RefreshToken logoutRefreshToken =
        refreshTokenRepository.findByToken(UUID.fromString(refreshToken.getValue())).orElse(null);
    assertThat(logoutRefreshToken).isNull();
  }

  @Test
  void shouldRefreshWithReturnStatusNoContent() throws Exception {
    LoginCookies loginCookies = getLoginCookies();

    MvcResult result =
        mockMvc
            .perform(
                post(AuthTestUtils.refreshPath())
                    .cookie(loginCookies.accessToken(), loginCookies.refreshToken()))
            .andExpect(status().isNoContent())
            .andExpect(cookie().exists(cookieUtils.getAccessTokenCookieName()))
            .andExpect(cookie().exists(cookieUtils.getRefreshTokenCookieName()))
            .andExpect(cookie().httpOnly(cookieUtils.getAccessTokenCookieName(), true))
            .andExpect(cookie().httpOnly(cookieUtils.getRefreshTokenCookieName(), true))
            .andExpect(cookie().path(cookieUtils.getAccessTokenCookieName(), "/"))
            .andExpect(cookie().path(cookieUtils.getRefreshTokenCookieName(), "/"))
            .andReturn();

    Cookie[] cookies = result.getResponse().getCookies();
    Cookie refreshToken =
        cookieUtils.getCookieByName(cookies, cookieUtils.getRefreshTokenCookieName());

    RefreshToken oldRefreshToken =
        refreshTokenRepository
            .findByToken(UUID.fromString(loginCookies.refreshToken().getValue()))
            .orElse(null);
    RefreshToken newRefreshToken =
        refreshTokenRepository.findByToken(UUID.fromString(refreshToken.getValue())).orElse(null);

    assertThat(oldRefreshToken).isNotNull();
    assertThat(newRefreshToken).isNotNull();
    assertThat(oldRefreshToken.getToken()).isNotEqualByComparingTo(newRefreshToken.getToken());
    assertThat(oldRefreshToken.isRevoked()).isTrue();
  }

  @Test
  void shouldRefreshWithReturnStatusNotFound() throws Exception {
    Cookie accessToken =
        cookieUtils.createHttpOnlyCookie(cookieUtils.getAccessTokenCookieName(), "", 60 * 60 * 24);
    Cookie refreshToken =
        cookieUtils.createHttpOnlyCookie(
            cookieUtils.getRefreshTokenCookieName(), UUID.randomUUID().toString(), 60 * 60 * 24);

    mockMvc
        .perform(post(AuthTestUtils.refreshPath()).cookie(accessToken, refreshToken))
        .andExpect(status().isNotFound());
  }

  private record LoginCookies(Cookie accessToken, Cookie refreshToken) {}

  private LoginCookies getLoginCookies() throws Exception {
    MvcResult loginResult =
        mockMvc
            .perform(
                post(AuthTestUtils.loginPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(AuthTestUtils.validLoginJson()))
            .andReturn();

    Cookie[] cookies = loginResult.getResponse().getCookies();
    Cookie accessToken =
        cookieUtils.getCookieByName(cookies, cookieUtils.getAccessTokenCookieName());
    Cookie refreshToken =
        cookieUtils.getCookieByName(cookies, cookieUtils.getRefreshTokenCookieName());

    return new LoginCookies(accessToken, refreshToken);
  }
}
