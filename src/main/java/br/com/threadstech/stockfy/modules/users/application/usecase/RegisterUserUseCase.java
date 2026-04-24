package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.application.dto.RegisterUserRequest;
import br.com.threadstech.stockfy.modules.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.modules.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.modules.users.domain.model.Email;
import br.com.threadstech.stockfy.modules.users.domain.model.Password;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void execute(RegisterUserRequest request) {
    if (request.confirmPassword() != null
        && !request.password().equals(request.confirmPassword())) {
      throw new PasswordMismatchException();
    }
    Email userEmail = new Email(request.email());
    if (userRepository.findByEmail(userEmail).isPresent()) {
      throw new EmailAlreadyExistsException();
    }

    User user =
        User.builder()
            .id(UUID.randomUUID())
            .name(request.name())
            .email(userEmail)
            .password(new Password(passwordEncoder.encode(request.password())))
            .role(request.role())
            .build();

    userRepository.save(user);
  }
}
