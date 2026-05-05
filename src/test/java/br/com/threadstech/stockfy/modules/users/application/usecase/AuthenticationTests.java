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

package br.com.threadstech.stockfy.modules.users.application.usecase;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.users.application.exception.InvalidRefreshTokenException;
import br.com.threadstech.stockfy.users.application.usecase.LoginUseCase;
import br.com.threadstech.stockfy.users.application.usecase.LogoutUseCase;
import br.com.threadstech.stockfy.users.application.usecase.RefreshTokenUseCase;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.users.infrastructure.messaging.RabbitMqEventPublisher;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AuthenticationTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private TokenService tokenService;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private RabbitMqEventPublisher eventPublisher;

	@InjectMocks
	private LoginUseCase loginUseCase;

	@InjectMocks
	private RefreshTokenUseCase refreshTokenUseCase;

	@InjectMocks
	private LogoutUseCase logoutUseCase;

	private User user;

	private final UUID userId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		this.user = this.user.builder()
			.id(this.userId)
			.name("John Doe")
			.email(new Email("john@example.com"))
			.password(new Password("hashed_password"))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

	@Test
	@DisplayName("Should login successfully")
	void shouldLoginSuccessfully() {
		given(this.userRepository.findByEmail(any())).willReturn(Optional.of(this.user));
		given(this.passwordEncoder.matches(any(), any())).willReturn(true);
		given(this.jwtService.generateToken(any(), any())).willReturn("access_token");
		given(this.tokenService.generateRefreshToken(any())).willReturn("refresh_token");

		var response = this.loginUseCase.execute("john@example.com", "password123");

		assertThat(response).isNotNull();
		assertThat(response.accessToken()).isEqualTo("access_token");
		assertThat(response.refreshToken()).isEqualTo("refresh_token");
	}

	@Test
	@DisplayName("Should throw exception for invalid credentials")
	void shouldThrowExceptionForInvalidCredentials() {
		given(this.userRepository.findByEmail(any())).willReturn(Optional.of(this.user));
		given(this.passwordEncoder.matches(any(), any())).willReturn(false);

		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		given(this.redisTemplate.opsForValue()).willReturn(valueOperations);

		assertThatExceptionOfType(InvalidCredentialsException.class)
			.isThrownBy(() -> this.loginUseCase.execute("john@example.com", "wrong"));
	}

	@Test
	@DisplayName("Should keep failed login attempts for one day")
	void execute_whenPasswordIsWrong_thenExpiresFailedAttemptsAfterOneDay() {
		given(this.userRepository.findByEmail(any())).willReturn(Optional.of(this.user));
		given(this.passwordEncoder.matches(any(), any())).willReturn(false);

		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		given(this.redisTemplate.opsForValue()).willReturn(valueOperations);

		assertThatExceptionOfType(InvalidCredentialsException.class)
			.isThrownBy(() -> this.loginUseCase.execute("john@example.com", "wrong"));
		verify(this.redisTemplate).expire(eq("login_attempts:john@example.com"), eq(Duration.ofDays(1)));
	}

	@Test
	@DisplayName("Should throw invalid credentials when email value object rejects address")
	void execute_whenEmailValueObjectRejectsAddress_thenThrowsInvalidCredentialsException() {
		assertThatExceptionOfType(InvalidCredentialsException.class)
			.isThrownBy(() -> this.loginUseCase.execute("john@example.technology", "password123"));
	}

	@Test
	@DisplayName("Should refresh token successfully")
	void shouldRefreshTokenSuccessfully() {
		given(this.tokenService.validateRefreshToken(any())).willReturn(true);
		given(this.tokenService.consumeRefreshToken("old_refresh_token")).willReturn(Optional.of(this.userId));
		given(this.userRepository.findById(any())).willReturn(Optional.of(this.user));
		given(this.jwtService.generateToken(any(), any())).willReturn("new_access_token");
		given(this.tokenService.generateRefreshToken(any())).willReturn("new_refresh_token");

		var response = this.refreshTokenUseCase.execute("old_refresh_token");

		assertThat(response.accessToken()).isEqualTo("new_access_token");
		assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
	}

	@Test
	@DisplayName("Should throw exception for invalid refresh token")
	void shouldThrowExceptionForInvalidRefreshToken() {
		given(this.tokenService.validateRefreshToken(any())).willReturn(false);

		assertThatExceptionOfType(InvalidRefreshTokenException.class)
			.isThrownBy(() -> this.refreshTokenUseCase.execute("invalid_refresh_token"));
	}

	@Test
	@DisplayName("Should throw invalid refresh token when user does not exist")
	void execute_whenRefreshTokenUserDoesNotExist_thenThrowsInvalidRefreshTokenException() {
		given(this.tokenService.validateRefreshToken("refresh_token")).willReturn(true);
		given(this.tokenService.consumeRefreshToken("refresh_token")).willReturn(Optional.of(this.userId));
		given(this.userRepository.findById(this.userId)).willReturn(Optional.empty());

		assertThatExceptionOfType(InvalidRefreshTokenException.class)
			.isThrownBy(() -> this.refreshTokenUseCase.execute("refresh_token"));
	}

	@Test
	@DisplayName("Should throw invalid refresh token when user is inactive")
	void execute_whenUserIsInactive_thenThrowsInvalidRefreshTokenException() {
		this.user.setActive(false);
		given(this.tokenService.validateRefreshToken("refresh_token")).willReturn(true);
		given(this.tokenService.consumeRefreshToken("refresh_token")).willReturn(Optional.of(this.userId));
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		assertThatExceptionOfType(InvalidRefreshTokenException.class)
			.isThrownBy(() -> this.refreshTokenUseCase.execute("refresh_token"));
	}

	@Test
	@DisplayName("Should throw invalid refresh token when user is locked")
	void execute_whenUserIsLocked_thenThrowsInvalidRefreshTokenException() {
		this.user.lock();
		given(this.tokenService.validateRefreshToken("refresh_token")).willReturn(true);
		given(this.tokenService.consumeRefreshToken("refresh_token")).willReturn(Optional.of(this.userId));
		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(this.user));

		assertThatExceptionOfType(InvalidRefreshTokenException.class)
			.isThrownBy(() -> this.refreshTokenUseCase.execute("refresh_token"));
	}

	@Test
	@DisplayName("Should logout and revoke token")
	void shouldLogoutSuccessfully() {
		this.logoutUseCase.execute("refresh_token");
		verify(this.tokenService).revokeRefreshToken("refresh_token");
	}

}
