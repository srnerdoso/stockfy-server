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
