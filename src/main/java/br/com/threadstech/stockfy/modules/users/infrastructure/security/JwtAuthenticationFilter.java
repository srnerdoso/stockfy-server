package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          token = cookie.getValue();
        }
      }
    }

    if (token != null) {
      try {
        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  UUID.fromString(userId),
                  null,
                  List.of(new SimpleGrantedAuthority(toAuthority(role))));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      } catch (Exception e) {
        // Invalid token, ignore
      }
    }

    filterChain.doFilter(request, response);
  }

  private String toAuthority(String role) {
    if (role.startsWith("ROLE_")) {
      return role;
    }

    return "ROLE_" + role;
  }
}
