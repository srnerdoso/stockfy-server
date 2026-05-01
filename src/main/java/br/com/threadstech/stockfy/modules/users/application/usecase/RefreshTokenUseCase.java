package br.com.threadstech.stockfy.modules.users.application.usecase;

import br.com.threadstech.stockfy.modules.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.modules.users.application.exception.InvalidRefreshTokenException;
import br.com.threadstech.stockfy.modules.users.domain.model.User;
import br.com.threadstech.stockfy.modules.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.modules.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.modules.users.infrastructure.security.TokenService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

  private final TokenService tokenService;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  public AuthResponse execute(String refreshToken) {
    if (refreshToken == null
        || refreshToken.isBlank()
        || !tokenService.validateRefreshToken(refreshToken)) {
      throw new InvalidRefreshTokenException();
    }

    UUID userId = consumeRefreshToken(refreshToken);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(InvalidRefreshTokenException::new);

    if (!user.isActive() || user.getStatus() == UserStatus.LOCKED) {
      throw new InvalidRefreshTokenException();
    }

    String accessToken = jwtService.generateToken(user.getId(), user.getRoles());
    String newRefreshToken = tokenService.generateRefreshToken(user.getId());

    return new AuthResponse(accessToken, newRefreshToken);
  }

  private UUID consumeRefreshToken(String refreshToken) {
    return tokenService
        .consumeRefreshToken(refreshToken)
        .orElseThrow(InvalidRefreshTokenException::new);
  }
}
