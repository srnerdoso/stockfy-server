package br.com.threadstech.stockfy.users.application.usecase;

import br.com.threadstech.stockfy.users.application.exception.CurrentPasswordInvalidException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordResetCodeException;
import br.com.threadstech.stockfy.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
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

	private final ResetCodeHasher resetCodeHasher;

	private final StringRedisTemplate redisTemplate;

	@Transactional
	public void executeWithCode(String code, String newPassword, String confirmPassword) {
		validatePasswordConfirmation(newPassword, confirmPassword);
		User user = findUserByResetCode(code);
		updatePassword(user, newPassword);
		user.setResetPasswordCodeHash(null);
		user.setResetPasswordExpiresAt(null);
		userRepository.update(user);
	}

	@Transactional(noRollbackFor = CurrentPasswordInvalidException.class)
	public void executeAuthenticated(UUID userId, String currentPassword, String newPassword, String confirmPassword) {
		validatePasswordConfirmation(newPassword, confirmPassword);
		User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

		if (currentPassword == null || currentPassword.isBlank()
				|| !passwordEncoder.matches(currentPassword, user.getPassword().value())) {
			countInvalidAttempt(user);
			throw new CurrentPasswordInvalidException();
		}

		redisTemplate.delete(INVALID_ATTEMPTS_KEY + user.getId());
		updatePassword(user, newPassword);
		userRepository.update(user);
	}

	private void validatePasswordConfirmation(String newPassword, String confirmPassword) {
		if (!newPassword.equals(confirmPassword)) {
			throw new PasswordMismatchException();
		}
	}

	private User findUserByResetCode(String code) {
		String codeHash = resetCodeHasher.hash(code);
		return userRepository.findByResetPasswordCodeHash(codeHash)
			.filter(this::resetCodeNotExpired)
			.orElseThrow(InvalidPasswordResetCodeException::new);
	}

	private boolean resetCodeNotExpired(User user) {
		return user.getResetPasswordExpiresAt() != null
				&& !user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now());
	}

	private void updatePassword(User user, String newPassword) {
		user.setPassword(new Password(passwordEncoder.encode(newPassword)));
	}

	private void countInvalidAttempt(User user) {
		String key = INVALID_ATTEMPTS_KEY + user.getId();
		Long attempts = redisTemplate.opsForValue().increment(key);
		redisTemplate.expire(key, Duration.ofMinutes(5));
		if (attempts != null && attempts >= MAX_INVALID_ATTEMPTS) {
			user.lock();
			userRepository.update(user);
		}
	}

}
