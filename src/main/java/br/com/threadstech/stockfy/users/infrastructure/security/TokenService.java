/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.threadstech.stockfy.users.infrastructure.security;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import br.com.threadstech.stockfy.config.SecurityProperties;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	private static final String USER_TOKENS_PREFIX = "user_tokens:";

	private final StringRedisTemplate redisTemplate;

	private final SecurityProperties securityProperties;

	private final HmacSha256Hasher hasher;

	public String generateRefreshToken(UUID userId) {
		String refreshToken = UUID.randomUUID().toString();
		String hashed = this.hasher.hash(refreshToken);
		this.redisTemplate.opsForValue()
			.set(REFRESH_TOKEN_PREFIX + hashed, userId.toString(),
					this.securityProperties.jwt().refreshTokenExpiration().toMillis(), TimeUnit.MILLISECONDS);
		
		String userKey = USER_TOKENS_PREFIX + userId;
		this.redisTemplate.opsForSet().add(userKey, hashed);
		this.redisTemplate.expire(userKey, this.securityProperties.jwt().refreshTokenExpiration().toMillis(), TimeUnit.MILLISECONDS);
		
		return refreshToken;
	}

  // Boolean.TRUE.equals(...) é intencional.
  // redisTemplate.hasKey(...) retorna Boolean nullable.
  // Remover isso pode causar NullPointerException.
	public boolean validateRefreshToken(String refreshToken) {
		return Boolean.TRUE.equals(this.redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + this.hasher.hash(refreshToken)));
	}

	public UUID getUserIdFromRefreshToken(String refreshToken) {
		String userId = this.redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + this.hasher.hash(refreshToken));
		return (userId != null) ? UUID.fromString(userId) : null;
	}

	public Optional<UUID> consumeRefreshToken(String refreshToken) {
		String hashed = this.hasher.hash(refreshToken);
		String userId = this.redisTemplate.opsForValue()
			.getAndDelete(REFRESH_TOKEN_PREFIX + hashed);
		if (userId == null) {
			return Optional.empty();
		}

		this.redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, hashed);

		try {
			return Optional.of(UUID.fromString(userId));
		}
		catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	public void revokeRefreshToken(String refreshToken) {
		String hashed = this.hasher.hash(refreshToken);
		String userId = this.redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + hashed);
		this.redisTemplate.delete(REFRESH_TOKEN_PREFIX + hashed);
		if (userId != null) {
			this.redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, hashed);
		}
	}

	public void revokeAllUserTokens(UUID userId) {
		String userKey = USER_TOKENS_PREFIX + userId;
		java.util.Set<String> hashedTokens = this.redisTemplate.opsForSet().members(userKey);
		if (hashedTokens != null && !hashedTokens.isEmpty()) {
			for (String hash : hashedTokens) {
				this.redisTemplate.delete(REFRESH_TOKEN_PREFIX + hash);
			}
			this.redisTemplate.delete(userKey);
		}
	}

}
