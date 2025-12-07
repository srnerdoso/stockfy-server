package br.com.threadstech.stockfy.security.jwt;

import br.com.threadstech.stockfy.api.ApiPaths;
import br.com.threadstech.stockfy.components.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  @Autowired private JwtUserDetailsService detailsService;
  @Autowired private JwtUtils jwtUtils;
  @Autowired private CookieUtils cookieUtils;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    log.info("JWT Filter - Request URI: {}", request.getRequestURI());

    if (request.getRequestURI().contains(ApiPaths.AUTH)) {
      log.info("JWT Filter - Request URI public. No authentication required.");
      filterChain.doFilter(request, response);
      return;
    }

    final String accTokenCookieName = cookieUtils.getAccessTokenCookieName();
    final Cookie cookieJwt = cookieUtils.getCookieByName(request.getCookies(), accTokenCookieName);

    if (cookieJwt == null) {
      log.info("No access token cookie found.");
      filterChain.doFilter(request, response);
      return;
    }

    final String accessToken = cookieJwt.getValue();

    if (accessToken == null || accessToken.isBlank()) {
      log.info("Access token is null or blank.");
      filterChain.doFilter(request, response);
      return;
    }

    if (!jwtUtils.isTokenValid(accessToken)) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token.");
      return;
    }

    String username = jwtUtils.getUsernameFromToken(accessToken);

    if (username == null || username.isBlank()) {
      log.info("Username is null or blank.");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid access token.");
      return;
    }

    toAuthentication(request, username);
    filterChain.doFilter(request, response);
  }

  private void toAuthentication(HttpServletRequest request, String username) {
    UserDetails userDetails = detailsService.loadUserByUsername(username);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
