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

import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.RateLimitFilter;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTests {

	private static final String CLIENT_IP = "127.0.0.1";

	@Mock
	private UserRateLimitConfig rateLimitConfig;

	@Mock
	private Bucket loginBucket;

	@Mock
	private Bucket generalBucket;

	@Mock
	private FilterChain filterChain;

	@Test
	@DisplayName("Deve usar bucket de login apenas para criacao de sessao")
	void doFilter_whenPostLoginEndpoint_thenUsesLoginBucket() throws Exception {
		given(this.rateLimitConfig.resolveLoginBucket(CLIENT_IP)).willReturn(this.loginBucket);
		given(this.loginBucket.tryConsume(1)).willReturn(true);

		new RateLimitFilter(this.rateLimitConfig).doFilter(loginRequest(), new MockHttpServletResponse(),
				this.filterChain);

		verify(this.rateLimitConfig).resolveLoginBucket(CLIENT_IP);
		verify(this.rateLimitConfig, never()).resolveGeneralBucket(anyString());
	}

	@Test
	@DisplayName("Deve usar bucket geral para renovacao de sessao")
	void doFilter_whenPostRefreshEndpoint_thenUsesGeneralBucket() throws Exception {
		given(this.rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).willReturn(this.generalBucket);
		given(this.generalBucket.tryConsume(1)).willReturn(true);

		new RateLimitFilter(this.rateLimitConfig).doFilter(refreshRequest(), new MockHttpServletResponse(),
				this.filterChain);

		verify(this.rateLimitConfig).resolveGeneralBucket(CLIENT_IP);
		verify(this.rateLimitConfig, never()).resolveLoginBucket(anyString());
	}

	@Test
	@DisplayName("Deve usar bucket geral para encerramento de sessao")
	void doFilter_whenDeleteCurrentSessionEndpoint_thenUsesGeneralBucket() throws Exception {
		given(this.rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).willReturn(this.generalBucket);
		given(this.generalBucket.tryConsume(1)).willReturn(true);

		new RateLimitFilter(this.rateLimitConfig).doFilter(logoutRequest(), new MockHttpServletResponse(),
				this.filterChain);

		verify(this.rateLimitConfig).resolveGeneralBucket(CLIENT_IP);
		verify(this.rateLimitConfig, never()).resolveLoginBucket(anyString());
	}

	@Test
	@DisplayName("Deve retornar 429 sem corpo para encerramento de sessao")
	void doFilter_whenLogoutLimitExceeded_thenReturns429WithoutBody() throws Exception {
		given(this.rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).willReturn(this.generalBucket);
		given(this.generalBucket.tryConsume(1)).willReturn(false);

		var response = new MockHttpServletResponse();
		new RateLimitFilter(this.rateLimitConfig).doFilter(logoutRequest(), response, this.filterChain);

		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getContentAsString()).isEqualTo("");
	}

	@Test
	@DisplayName("Deve preservar corpo de rate limit para demais endpoints")
	void doFilter_whenNonLogoutLimitExceeded_thenReturns429WithMessage() throws Exception {
		given(this.rateLimitConfig.resolveGeneralBucket(CLIENT_IP)).willReturn(this.generalBucket);
		given(this.generalBucket.tryConsume(1)).willReturn(false);

		var response = new MockHttpServletResponse();
		new RateLimitFilter(this.rateLimitConfig).doFilter(refreshRequest(), response, this.filterChain);

		assertThat(response.getStatus()).isEqualTo(429);
		assertThat(response.getContentAsString()).isEqualTo("Too many requests");
	}

	private MockHttpServletRequest loginRequest() {
		var request = new MockHttpServletRequest("POST", "/api/v1/auth/sessions");
		request.setRemoteAddr(CLIENT_IP);
		return request;
	}

	private MockHttpServletRequest refreshRequest() {
		var request = new MockHttpServletRequest("POST", "/api/v1/auth/sessions/refresh");
		request.setRemoteAddr(CLIENT_IP);
		return request;
	}

	private MockHttpServletRequest logoutRequest() {
		var request = new MockHttpServletRequest("DELETE", "/api/v1/auth/sessions/current");
		request.setRemoteAddr(CLIENT_IP);
		return request;
	}

}
