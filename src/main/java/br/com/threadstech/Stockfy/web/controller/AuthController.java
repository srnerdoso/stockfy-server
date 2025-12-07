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
import br.com.threadstech.stockfy.web.dto.LoginDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

  private final JwtUtils jwtUtils;
  private final CookieUtils cookieUtils;
  private final EmployeeService employeeService;
  private final RefreshTokenService refreshTokenService;
  private final JwtUserDetailsService jwtUserDetailsService;
  private final AuthenticationManager authenticationManager;

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
      UUID refreshToken = refreshTokenService.save(loginDto.getDeviceId(), employee).getToken();
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
    Cookie[] cookies = request.getCookies();
    String refreshToken =
        cookieUtils
            .getCookieByName(cookies, "refresh_token")
            .getValue(); // Verificação de nulidade já é feita no filtro
    RefreshToken newRefreshToken = refreshTokenService.refresh(refreshToken);

    Employee employee = newRefreshToken.getEmployee();
    JwtToken jwtToken =
        jwtUserDetailsService.getTokenAuthenticated(
            employee.getId(), employee.getContact().getEmail(), employee.getRole().name());

    addCookies(response, jwtToken, newRefreshToken.getToken());
    return ResponseEntity.noContent().build();
  }

  // TODO: Logout

  private void addCookies(HttpServletResponse response, JwtToken jwtToken, UUID refreshToken) {
    int expireDays = jwtUtils.getExpireDays();
    int expireHours = jwtUtils.getExpireHours();
    int expireMinutes = jwtUtils.getExpireMinutes();

    int maxAge = (expireDays * 24 * 60 * 60) + (expireHours * 60 * 60) + (expireMinutes * 60);
    String accessTokenName = cookieUtils.getAccessTokenCookieName();
    String refreshTokenName = cookieUtils.getRefreshTokenCookieName();

    response.addCookie(
        cookieUtils.createHttpOnlyCookie(accessTokenName, jwtToken.getToken(), maxAge));
    response.addCookie(
        cookieUtils.createHttpOnlyCookie(refreshTokenName, refreshToken.toString(), maxAge));
  }
}
