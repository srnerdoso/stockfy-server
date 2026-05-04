package br.com.threadstech.stockfy.users.infrastructure.security;

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

	private final TokenService tokenService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String accessToken = null;
		String refreshToken = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("access_token".equals(cookie.getName())) {
					accessToken = cookie.getValue();
				}
				if ("refresh_token".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
				}
			}
		}

		if (accessToken != null && refreshToken != null) {
			try {
				String userId = jwtService.extractUserId(accessToken);
				List<String> roles = jwtService.extractRoles(accessToken);
				UUID authenticatedUserId = UUID.fromString(userId);
				UUID refreshTokenUserId = tokenService.getUserIdFromRefreshToken(refreshToken);

				if (tokenService.validateRefreshToken(refreshToken) && authenticatedUserId.equals(refreshTokenUserId)
						&& SecurityContextHolder.getContext().getAuthentication() == null) {
					List<SimpleGrantedAuthority> authorities = roles.stream()
						.map(this::toAuthority)
						.map(SimpleGrantedAuthority::new)
						.toList();

					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							authenticatedUserId, null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
			catch (Exception e) {
				SecurityContextHolder.clearContext();
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
