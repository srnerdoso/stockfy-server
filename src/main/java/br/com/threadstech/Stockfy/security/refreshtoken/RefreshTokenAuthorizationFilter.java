package br.com.threadstech.stockfy.security.refreshtoken;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.components.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// IMPORTANTE: Deixe este filtro depois do filtro JWT

@Slf4j
public class RefreshTokenAuthorizationFilter extends OncePerRequestFilter {

  @Autowired private CookieUtils cookieUtils;
  @Autowired private RefreshTokenService refreshTokenService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String requestUri = request.getRequestURI();
    log.info("Refresh Token Filter - Request URI: {}", requestUri);

    List<String> unauthenticatedPaths =
        List.of(ApiPaths.AUTH_V1 + "/login", ApiPaths.AUTH_V1 + "/logout", ApiPaths.AUTH_V1 + "/refresh");
    if (unauthenticatedPaths.contains(requestUri)) {
      log.info("Refresh Token Filter - Request URI public. No authentication required.");
      filterChain.doFilter(request, response);
      return;
    }

    Cookie refTokenCookie = cookieUtils.getCookieByName(request.getCookies(), "ref_token");
    if (refTokenCookie == null) {
      log.info("Refresh Token Filter - No cookie refresh token found.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token.");
      return;
    }

    String refreshTokenStr = refTokenCookie.getValue();

    if (refreshTokenStr == null) {
      log.info("Refresh Token Filter - Refresh token is null.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token.");
      return;
    }

    UUID refreshTokenUuid = UUID.fromString(refreshTokenStr);
    String json = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
    var mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(json);
    String deviceId = jsonNode.has("deviceId") ? jsonNode.get("deviceId").asString() : null;

    if (deviceId == null) {
      log.info("Refresh Token Filter - Device id is null.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized device.");
      return;
    }

    if (!refreshTokenService.isTokenValid(refreshTokenUuid, deviceId)) {
      log.info("Refresh Token Filter - Invalid refresh token.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token.");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
