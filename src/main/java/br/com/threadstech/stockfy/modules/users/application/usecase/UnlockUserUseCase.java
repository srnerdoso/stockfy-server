package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UnlockUserUseCase {

  private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";

  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;

  @Transactional
  public void execute(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    user.unlock();
    redisTemplate.delete(LOGIN_ATTEMPTS_KEY + user.getEmail().value());
    userRepository.update(user);
  }
}
