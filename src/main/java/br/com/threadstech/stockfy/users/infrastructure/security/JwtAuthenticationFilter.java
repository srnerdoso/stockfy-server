/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.threadstech.stockfy.users.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
