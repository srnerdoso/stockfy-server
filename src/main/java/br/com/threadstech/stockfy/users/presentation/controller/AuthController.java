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

package br.com.threadstech.stockfy.users.presentation.controller;

import br.com.threadstech.stockfy.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.users.application.dto.LoginRequest;
import br.com.threadstech.stockfy.users.application.usecase.LoginUseCase;
import br.com.threadstech.stockfy.users.application.usecase.LogoutUseCase;
import br.com.threadstech.stockfy.users.application.usecase.RefreshTokenUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
public class AuthController {

	private static final String ACCESS_TOKEN_COOKIE = "access_token";

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private final LoginUseCase loginUseCase;

	private final RefreshTokenUseCase refreshTokenUseCase;

	private final LogoutUseCase logoutUseCase;

	@PostMapping
	public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
		AuthResponse authResponse = this.loginUseCase.execute(request.email(), request.password());
		addCookies(response, authResponse);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
			HttpServletResponse response) {
		AuthResponse authResponse = this.refreshTokenUseCase.execute(refreshToken);
		addCookies(response, authResponse);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/current")
	public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE) String refreshToken,
			HttpServletResponse response) {
		this.logoutUseCase.execute(refreshToken);
		clearCookies(response);
		return ResponseEntity.noContent().build();
	}

	private void addCookies(HttpServletResponse response, AuthResponse authResponse) {
		Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, authResponse.accessToken());
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true); // Should be true in prod
		accessCookie.setPath("/");
		accessCookie.setMaxAge(900); // 15 min

		Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, authResponse.refreshToken());
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(604800); // 7 days

		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}

	private void clearCookies(HttpServletResponse response) {
		Cookie accessCookie = expiredCookie(ACCESS_TOKEN_COOKIE);
		Cookie refreshCookie = expiredCookie(REFRESH_TOKEN_COOKIE);
		response.addCookie(accessCookie);
		response.addCookie(refreshCookie);
	}

	private Cookie expiredCookie(String name) {
		Cookie cookie = new Cookie(name, null);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		return cookie;
	}

}
