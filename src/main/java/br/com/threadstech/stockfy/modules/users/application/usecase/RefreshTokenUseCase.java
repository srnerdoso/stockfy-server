package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

  private final TokenService tokenService;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  public AuthResponse execute(String refreshToken) {
    if (!tokenService.validateRefreshToken(refreshToken)) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    UUID userId = tokenService.getUserIdFromRefreshToken(refreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    String accessToken = jwtService.generateToken(user.getId(), user.getRole().name());

    return new AuthResponse(accessToken, refreshToken);
  }
}
