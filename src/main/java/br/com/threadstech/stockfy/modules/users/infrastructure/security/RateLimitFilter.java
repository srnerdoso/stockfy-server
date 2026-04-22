package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import br.com.threadstech.stockfy.modules.users.infrastructure.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitConfig rateLimitConfig;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String clientIp = request.getRemoteAddr();

    Bucket bucket;
    if (path.contains("/auth/sessions")) {
      bucket = rateLimitConfig.resolveLoginBucket(clientIp);
    } else {
      bucket = rateLimitConfig.resolveGeneralBucket(clientIp);
    }

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.getWriter().write("Too many requests");
    }
  }
}
