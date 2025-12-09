package br.com.threadstech.stockfy.web.controller;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.components.CookieUtils;
import br.com.threadstech.stockfy.entity.Employee;
import br.com.threadstech.stockfy.security.jwt.JwtToken;
import br.com.threadstech.stockfy.security.jwt.JwtUserDetailsService;
import br.com.threadstech.stockfy.security.jwt.JwtUtils;
import br.com.threadstech.stockfy.security.refreshtoken.RefreshToken;
import br.com.threadstech.stockfy.security.refreshtoken.RefreshTokenService;
import br.com.threadstech.stockfy.service.EmployeeService;
import br.com.threadstech.stockfy.web.doc.AuthControllerDoc;
import br.com.threadstech.stockfy.web.dto.LoginDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.AUTH)
public class AuthController implements AuthControllerDoc {

  private final JwtUtils jwtUtils;
  private final CookieUtils cookieUtils;
  private final EmployeeService employeeService;
  private final RefreshTokenService refreshTokenService;
  private final JwtUserDetailsService jwtUserDetailsService;
  private final AuthenticationManager authenticationManager;

  @Value("${refresh-token.expire-days}")
  private int refreshTokenExpireDays;

  @PostMapping("/login")
  public ResponseEntity<Void> auth(
      @Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
    try {
      String username = loginDto.getEmail();
      String password = loginDto.getPassword();
      Employee employee = employeeService.findByEmail(username);
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));

      JwtToken jwtToken =
          jwtUserDetailsService.getTokenAuthenticated(
              employee.getId(), employee.getContact().getEmail(), employee.getRole().name());
      UUID refreshToken =
          refreshTokenService
              .save(loginDto.getDeviceId(), employee, refreshTokenExpireDays)
              .getToken();
      addCookies(response, jwtToken, refreshToken);

      log.info("Authentication successful.");
      return ResponseEntity.noContent().build();
    } catch (AuthenticationException ex) {
      log.error("Authentication failed: ", ex);
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    String refreshToken =
        getRefreshTokenCookieValue(
            request); // Verificação de nulidade já é feita no filtro para este endpoint
    RefreshToken newRefreshToken =
        refreshTokenService.refresh(refreshToken, refreshTokenExpireDays);

    Employee employee = newRefreshToken.getEmployee();
    JwtToken jwtToken =
        jwtUserDetailsService.getTokenAuthenticated(
            employee.getId(), employee.getContact().getEmail(), employee.getRole().name());

    addCookies(response, jwtToken, newRefreshToken.getToken());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = getRefreshTokenCookieValue(request);
    if (refreshToken == null) {
      return ResponseEntity.noContent().build();
    }
    refreshTokenService.deleteByToken(refreshToken);
    deleteCookies(response);
    return ResponseEntity.noContent().build();
  }

  private void addCookies(HttpServletResponse response, JwtToken jwtToken, UUID refreshToken) {
    int expireDays = jwtUtils.getExpireDays();
    int expireHours = jwtUtils.getExpireHours();
    int expireMinutes = jwtUtils.getExpireMinutes();

    int accessTokenMaxAge =
        (expireDays * 24 * 60 * 60) + (expireHours * 60 * 60) + (expireMinutes * 60);
    String accessTokenName = cookieUtils.getAccessTokenCookieName();

    int refreshTokenMaxAge = refreshTokenExpireDays * 24 * 60 * 60;
    String refreshTokenName = cookieUtils.getRefreshTokenCookieName();

    response.addCookie(
        cookieUtils.createHttpOnlyCookie(accessTokenName, jwtToken.getToken(), accessTokenMaxAge));
    response.addCookie(
        cookieUtils.createHttpOnlyCookie(
            refreshTokenName, refreshToken.toString(), refreshTokenMaxAge));
  }

  private void deleteCookies(HttpServletResponse response) {
    Cookie refreshDelete = cookieUtils.deleteCookie(cookieUtils.getRefreshTokenCookieName(), "");
    Cookie accessDelete = cookieUtils.deleteCookie(cookieUtils.getAccessTokenCookieName(), "");
    response.addCookie(refreshDelete);
    response.addCookie(accessDelete);
  }

  private String getRefreshTokenCookieValue(HttpServletRequest request) {
    return Optional.ofNullable(request.getCookies())
        .map(
            cookies ->
                cookieUtils.getCookieByName(cookies, cookieUtils.getRefreshTokenCookieName()))
        .map(Cookie::getValue)
        .orElse(null);
  }
}
