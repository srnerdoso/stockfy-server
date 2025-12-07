package br.com.threadstech.stockfy.security.refreshtoken;

import br.com.threadstech.stockfy.entity.Employee;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public RefreshToken save(String deviceId, Employee employee) {
    log.info("Saving refresh token...");
    UUID token = UUID.randomUUID();

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setDeviceId(deviceId);
    refreshToken.setEmployee(employee);

    refreshToken.setDeviceId(passwordEncoder.encode(refreshToken.getDeviceId()));
    RefreshToken saved = refreshTokenRepository.save(refreshToken);
    log.info("Refresh token saved successfully.");
    return saved;
  }

  @Transactional
  public RefreshToken revokeByToken(UUID token) {
    log.info("Revoking refresh token...");
    RefreshToken refreshToken = findByToken(token);
    refreshToken.setRevoked(true);
    log.info("Refresh token revoked successfully.");
    return refreshToken;
  }

  @Transactional(readOnly = true)
  public RefreshToken findByToken(UUID token) {
    log.info("Finding refresh token by token...");
    RefreshToken refreshToken =
        refreshTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Token"));
    log.info("Refresh token found successfully.");
    return refreshToken;
  }

  @Transactional(readOnly = true)
  public boolean isTokenValid(UUID token, String deviceId) {
    RefreshToken refreshToken = findByToken(token);

    log.info("Checking refresh token validity...");
    if (refreshToken.getDeviceId() == null) {
      log.error("Unauthorized access: device id is null.");
      return false;
    }
    if (!passwordEncoder.matches(deviceId, refreshToken.getDeviceId())) {
      log.error("Unauthorized access: device id does not match.");
      return false;
    }
    if (refreshToken.isRevoked()) {
      log.error("Unauthorized access: token is revoked.");
      return false;
    }
    log.info("Access allowed: refresh token is valid.");

    return true;
  }

  public RefreshToken refresh(String refreshToken) {
    log.info("Refreshing refresh token...");
    UUID token = UUID.fromString(refreshToken);
    RefreshToken revoked = revokeByToken(token);
    RefreshToken newToken = save(revoked.getDeviceId(), revoked.getEmployee());
    log.info("Refresh token refreshed successfully.");
    return newToken;
  }
}
