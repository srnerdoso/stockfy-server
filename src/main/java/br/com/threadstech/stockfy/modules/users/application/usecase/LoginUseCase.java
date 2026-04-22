package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.application.dto.AuthResponse;
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

    public AuthResponse execute(String email, String password) {
        User user = userRepository.findByEmail(new Email(email))
                .filter(u -> u.isActive() && u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials or account locked"));

        if (!passwordEncoder.matches(password, user.getPassword().value())) {
            // Note: Rate limit check and lock logic will be in Task 10
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getId(), user.getRole().name());
        String refreshToken = tokenService.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken);
    }
}
