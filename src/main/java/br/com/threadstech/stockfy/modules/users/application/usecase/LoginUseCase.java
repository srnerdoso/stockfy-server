package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.modules.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.TokenService;
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
  private final br.com.threadstech.stockfy.modules.users.infrastructure.messaging
          .RabbitMqEventPublisher
      eventPublisher;

  private static final int MAX_FAILED_ATTEMPTS = 15;
  private static final String FAILED_ATTEMPTS_KEY = "login_attempts:";

  public AuthResponse execute(String email, String password) {
    User user =
        userRepository
            .findByEmail(new Email(email))
            .orElseThrow(InvalidCredentialsException::new);

    if (!user.isActive() || user.getStatus() == UserStatus.LOCKED) {
      throw new InvalidCredentialsException();
    }

    if (!passwordEncoder.matches(password, user.getPassword().value())) {
      handleFailedLogin(user);
      throw new InvalidCredentialsException();
    }

    resetFailedAttempts(email);

    String accessToken = jwtService.generateToken(user.getId(), user.getRoles());
    String refreshToken = tokenService.generateRefreshToken(user.getId());

    return new AuthResponse(accessToken, refreshToken);
  }

  private void handleFailedLogin(User user) {
    String key = FAILED_ATTEMPTS_KEY + user.getEmail().value();
    Long attempts = redisTemplate.opsForValue().increment(key);
    redisTemplate.expire(key, java.time.Duration.ofMinutes(15));

    if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
      user.lock();
      userRepository.update(user);
      eventPublisher.publish(
          new br.com.threadstech.stockfy.modules.users.domain.event.AccountLockedEvent(
              user.getId(), user.getEmail().value(), "Max failed attempts exceeded"));
    }
  }

  private void resetFailedAttempts(String email) {
    redisTemplate.delete(FAILED_ATTEMPTS_KEY + email);
  }
}
