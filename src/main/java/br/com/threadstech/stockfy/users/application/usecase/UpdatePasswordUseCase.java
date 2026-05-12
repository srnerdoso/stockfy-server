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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.CurrentPasswordInvalidException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordResetCodeException;
import br.com.threadstech.stockfy.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.users.infrastructure.security.HmacSha256Hasher;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdatePasswordUseCase {

	private static final int MAX_INVALID_ATTEMPTS = 15;

	private static final String INVALID_ATTEMPTS_KEY = "password_update_attempts:";

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final HmacSha256Hasher resetCodeHasher;

	private final StringRedisTemplate redisTemplate;

	@Transactional
	public void executeWithCode(String code, String newPassword, String confirmPassword) {
		validatePasswordConfirmation(newPassword, confirmPassword);
		User user = findUserByResetCode(code);
		updatePassword(user, newPassword);
		user.setResetPasswordCodeHash(null);
		user.setResetPasswordExpiresAt(null);
		this.userRepository.update(user);
	}

	@Transactional(noRollbackFor = CurrentPasswordInvalidException.class)
	public void executeAuthenticated(UUID userId, String currentPassword, String newPassword, String confirmPassword) {
		validatePasswordConfirmation(newPassword, confirmPassword);
		User user = this.userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

		if (currentPassword == null || currentPassword.isBlank()
				|| !this.passwordEncoder.matches(currentPassword, user.getPassword().value())) {
			countInvalidAttempt(user);
			throw new CurrentPasswordInvalidException();
		}

		this.redisTemplate.delete(INVALID_ATTEMPTS_KEY + user.getId());
		updatePassword(user, newPassword);
		this.userRepository.update(user);
	}

	private void validatePasswordConfirmation(String newPassword, String confirmPassword) {
		if (!newPassword.equals(confirmPassword)) {
			throw new PasswordMismatchException();
		}
	}

	private User findUserByResetCode(String code) {
		String codeHash = this.resetCodeHasher.hash(code);
		return this.userRepository.findByResetPasswordCodeHash(codeHash)
			.filter(this::resetCodeNotExpired)
			.orElseThrow(InvalidPasswordResetCodeException::new);
	}

	private boolean resetCodeNotExpired(User user) {
		return user.getResetPasswordExpiresAt() != null
				&& !user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now());
	}

	private void updatePassword(User user, String newPassword) {
		user.setPassword(new Password(this.passwordEncoder.encode(newPassword)));
	}

	private void countInvalidAttempt(User user) {
		String key = INVALID_ATTEMPTS_KEY + user.getId();
		Long attempts = this.redisTemplate.opsForValue().increment(key);
		this.redisTemplate.expire(key, Duration.ofMinutes(5));
		if (attempts != null && attempts >= MAX_INVALID_ATTEMPTS) {
			user.lock();
			this.userRepository.update(user);
		}
	}

}
