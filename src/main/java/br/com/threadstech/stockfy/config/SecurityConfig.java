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

package br.com.threadstech.stockfy.config;

import br.com.threadstech.stockfy.users.infrastructure.security.JwtAuthenticationFilter;
import br.com.threadstech.stockfy.users.infrastructure.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final RateLimitFilter rateLimitFilter;

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	private final UnauthorizedAuthenticationEntryPoint unauthorizedAuthenticationEntryPoint;

	private final ForbiddenAccessDeniedResponder forbiddenAccessDeniedResponder;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(
					(exception) -> exception.authenticationEntryPoint(this.unauthorizedAuthenticationEntryPoint)
						.accessDeniedHandler(this.forbiddenAccessDeniedResponder))
			.addFilterBefore(this.rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(this.jwtAuthenticationFilter, RateLimitFilter.class)
			.authorizeHttpRequests((auth) -> auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/sessions/**")
				.permitAll()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/users/password")
				.permitAll()
				.requestMatchers("/api/v1/**")
				.authenticated()
				.anyRequest()
				.permitAll());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
