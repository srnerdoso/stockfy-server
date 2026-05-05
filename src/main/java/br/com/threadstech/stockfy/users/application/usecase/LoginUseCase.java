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

package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.users.domain.event.AccountLockedEvent;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.users.infrastructure.messaging.RabbitMqEventPublisher;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	private final TokenService tokenService;

	private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

	private final RabbitMqEventPublisher eventPublisher;

	private static final int MAX_FAILED_ATTEMPTS = 15;

	private static final String FAILED_ATTEMPTS_KEY = "login_attempts:";

	public AuthResponse execute(String email, String password) {
		Email userEmail = parseEmail(email);
		User user = this.userRepository.findByEmail(userEmail).orElseThrow(InvalidCredentialsException::new);

		if (!user.isActive() || user.getStatus() == UserStatus.LOCKED) {
			throw new InvalidCredentialsException();
		}

		if (!this.passwordEncoder.matches(password, user.getPassword().value())) {
			handleFailedLogin(user);
			throw new InvalidCredentialsException();
		}

		resetFailedAttempts(email);

		String accessToken = this.jwtService.generateToken(user.getId(), user.getRoles());
		String refreshToken = this.tokenService.generateRefreshToken(user.getId());

		return new AuthResponse(accessToken, refreshToken);
	}

	private void handleFailedLogin(User user) {
		String key = FAILED_ATTEMPTS_KEY + user.getEmail().value();
		Long attempts = this.redisTemplate.opsForValue().increment(key);
		this.redisTemplate.expire(key, java.time.Duration.ofDays(1));

		if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
			user.lock();
			this.userRepository.update(user);
			this.eventPublisher
				.publish(new AccountLockedEvent(user.getId(), user.getEmail().value(), "Max failed attempts exceeded"));
		}
	}

	private void resetFailedAttempts(String email) {
		this.redisTemplate.delete(FAILED_ATTEMPTS_KEY + email);
	}

	private Email parseEmail(String email) {
		try {
			return new Email(email);
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidCredentialsException();
		}
	}

}
