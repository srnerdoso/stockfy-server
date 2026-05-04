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

package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtAuthenticationFilter;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

	private final TokenService tokenService = org.mockito.Mockito.mock(TokenService.class);

	private final JwtService jwtService = new JwtService();

	private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, tokenService);

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Deve criar uma authority para cada role presente no token")
	void doFilterInternal_whenTokenHasMultipleRoles_thenCreatesAuthorityForEachRole() throws Exception {
		UUID userId = UUID.randomUUID();
		ReflectionTestUtils.setField(jwtService, "secret",
				"9a4f2c8d3b7a1e5f8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1d");
		ReflectionTestUtils.setField(jwtService, "expiration", 900000L);
		String accessToken = jwtService.generateToken(userId, Set.of(UserRole.ADMIN, UserRole.USER));
		String refreshToken = "refresh-token";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("access_token", accessToken), new Cookie("refresh_token", refreshToken));

		when(tokenService.getUserIdFromRefreshToken(refreshToken)).thenReturn(userId);
		when(tokenService.validateRefreshToken(refreshToken)).thenReturn(true);

		filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

		var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
		assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
	}

}
