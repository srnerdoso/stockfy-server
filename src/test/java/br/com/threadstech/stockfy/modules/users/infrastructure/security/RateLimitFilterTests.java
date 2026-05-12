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

import br.com.threadstech.stockfy.users.infrastructure.config.RateLimitDecision;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.RateLimitFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTests {

	private static final String CLIENT_IP = "127.0.0.1";

	private static final String POST = "POST";

	private static final String REFRESH_PATH = "/api/v1/auth/sessions/refresh";

	@Mock
	private UserRateLimitConfig rateLimitConfig;

	@Mock
	private FilterChain filterChain;

	@Test
	@DisplayName("Deve delegar decisao de rate limit para configuracao")
	void doFilter_whenRequestArrives_thenDelegatesDecisionToConfig() throws Exception {
		MockHttpServletRequest request = refreshRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		given(this.rateLimitConfig.consume(POST, REFRESH_PATH, CLIENT_IP)).willReturn(RateLimitDecision.allow());

		new RateLimitFilter(this.rateLimitConfig).doFilter(request, response, this.filterChain);

		verify(this.rateLimitConfig).consume(POST, REFRESH_PATH, CLIENT_IP);
		verify(this.filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("Deve retornar 429 sem corpo para encerramento de sessao")
	void doFilter_whenLogoutLimitExceeded_thenReturns429WithoutBody() throws Exception {
		given(this.rateLimitConfig.consume("DELETE", "/api/v1/auth/sessions/current", CLIENT_IP))
			.willReturn(RateLimitDecision.reject(false));

		var response = new MockHttpServletResponse();
		new RateLimitFilter(this.rateLimitConfig).doFilter(logoutRequest(), response, this.filterChain);

		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getContentAsString()).isEqualTo("");
	}

	@Test
	@DisplayName("Deve preservar corpo de rate limit para demais endpoints")
	void doFilter_whenNonLogoutLimitExceeded_thenReturns429WithMessage() throws Exception {
		given(this.rateLimitConfig.consume(POST, REFRESH_PATH, CLIENT_IP)).willReturn(RateLimitDecision.reject(true));

		var response = new MockHttpServletResponse();
		new RateLimitFilter(this.rateLimitConfig).doFilter(refreshRequest(), response, this.filterChain);

		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getContentAsString()).isEqualTo("Too many requests");
	}

	private MockHttpServletRequest refreshRequest() {
		var request = new MockHttpServletRequest(POST, REFRESH_PATH);
		request.setRemoteAddr(CLIENT_IP);
		return request;
	}

	private MockHttpServletRequest logoutRequest() {
		var request = new MockHttpServletRequest("DELETE", "/api/v1/auth/sessions/current");
		request.setRemoteAddr(CLIENT_IP);
		return request;
	}

}
