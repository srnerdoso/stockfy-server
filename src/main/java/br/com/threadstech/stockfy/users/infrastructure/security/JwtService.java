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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import br.com.threadstech.stockfy.config.SecurityProperties;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecurityProperties securityProperties;

	public JwtService(SecurityProperties securityProperties) {
		this.securityProperties = securityProperties;
	}

	public String generateToken(UUID userId, Set<UserRole> roles) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", roles.stream().map(UserRole::name).toList());
		return createToken(claims, userId.toString());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		Instant now = Instant.now();
		return Jwts.builder()
			.claims(claims)
			.subject(subject)
			.issuedAt(java.util.Date.from(now))
			.expiration(java.util.Date.from(now.plus(this.securityProperties.jwt().accessTokenExpiration())))
			.signWith(getSigningKey())
			.compact();
	}

	public boolean isTokenValid(String token, UUID userId) {
		final String username = extractUserId(token);
		return (username.equals(userId.toString())) && !isTokenExpired(token);
	}

	public String extractUserId(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public List<String> extractRoles(String token) {
		Object roles = extractAllClaims(token).get("roles");
		if (roles instanceof List<?> rawRoles) {
			return rawRoles.stream().map(String.class::cast).toList();
		}
		return List.of();
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().toInstant().isBefore(Instant.now());
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(this.securityProperties.jwt().secret().getBytes(StandardCharsets.UTF_8));
	}

}
