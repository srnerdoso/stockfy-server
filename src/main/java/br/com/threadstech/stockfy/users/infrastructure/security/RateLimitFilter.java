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

import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

	private final UserRateLimitConfig rateLimitConfig;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		String clientIp = request.getRemoteAddr();

		Bucket bucket;
		if ("POST".equals(request.getMethod()) && path.equals("/api/v1/auth/sessions")) {
			bucket = rateLimitConfig.resolveLoginBucket(clientIp);
		}
		else if (path.equals("/api/v1/users/password")) {
			bucket = rateLimitConfig.resolvePasswordBucket(clientIp);
		}
		else {
			bucket = rateLimitConfig.resolveGeneralBucket(clientIp);
		}

		if (bucket.tryConsume(1)) {
			filterChain.doFilter(request, response);
		}
		else {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			if (!isLogoutRequest(request)) {
				response.getWriter().write("Too many requests");
			}
		}
	}

	private boolean isLogoutRequest(HttpServletRequest request) {
		return "DELETE".equals(request.getMethod()) && "/api/v1/auth/sessions/current".equals(request.getRequestURI());
	}

}
