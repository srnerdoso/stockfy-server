package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void execute(String email, String code, String newPassword) {
    User user =
        userRepository
            .findByEmail(new Email(email))
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or code"));

    if (user.getResetPasswordExpiresAt() == null
        || user.getResetPasswordExpiresAt().isBefore(LocalDateTime.now())
        || !passwordEncoder.matches(code, user.getResetPasswordCodeHash())) {
      throw new IllegalArgumentException("Invalid email or code");
    }

    user.setPassword(new Password(passwordEncoder.encode(newPassword)));
    user.setResetPasswordCodeHash(null);
    user.setResetPasswordExpiresAt(null);

    userRepository.update(user);
  }
}
