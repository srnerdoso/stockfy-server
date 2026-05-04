package br.com.threadstech.stockfy.users.infrastructure.security;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final StringRedisTemplate redisTemplate;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	public String generateRefreshToken(UUID userId) {
		String refreshToken = UUID.randomUUID().toString();
		redisTemplate.opsForValue()
			.set("refresh_token:" + refreshToken, userId.toString(), refreshTokenExpiration, TimeUnit.MILLISECONDS);
		return refreshToken;
	}

	public boolean validateRefreshToken(String refreshToken) {
		return Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken));
	}

	public UUID getUserIdFromRefreshToken(String refreshToken) {
		String userId = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
		return userId != null ? UUID.fromString(userId) : null;
	}

	public Optional<UUID> consumeRefreshToken(String refreshToken) {
		String userId = redisTemplate.opsForValue().getAndDelete("refresh_token:" + refreshToken);
		if (userId == null) {
			return Optional.empty();
		}

		try {
			return Optional.of(UUID.fromString(userId));
		}
		catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	public void revokeRefreshToken(String refreshToken) {
		redisTemplate.delete("refresh_token:" + refreshToken);
	}

}
