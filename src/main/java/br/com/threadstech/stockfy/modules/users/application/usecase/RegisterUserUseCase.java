package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.domain.model.*;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void execute(String name, String email, String password, UserRole role) {
        Email userEmail = new Email(email);
        if (userRepository.findByEmail(userEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(userEmail)
                .password(new Password(passwordEncoder.encode(password)))
                .role(role)
                .build();

        userRepository.save(user);
    }
}
