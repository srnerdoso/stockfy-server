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

package br.com.threadstech.stockfy.users.application.usecase;

import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.AuthResponse;
import br.com.threadstech.stockfy.users.application.exception.InvalidRefreshTokenException;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

	private final TokenService tokenService;

	private final JwtService jwtService;

	private final UserRepository userRepository;

	public AuthResponse execute(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank() || !this.tokenService.validateRefreshToken(refreshToken)) {
			throw new InvalidRefreshTokenException();
		}

		UUID userId = consumeRefreshToken(refreshToken);
		User user = this.userRepository.findById(userId).orElseThrow(InvalidRefreshTokenException::new);

		if (!user.isActive() || user.getStatus() == UserStatus.LOCKED) {
			throw new InvalidRefreshTokenException();
		}

		String accessToken = this.jwtService.generateToken(user.getId(), user.getRoles());
		String newRefreshToken = this.tokenService.generateRefreshToken(user.getId());

		return new AuthResponse(accessToken, newRefreshToken);
	}

	private UUID consumeRefreshToken(String refreshToken) {
		return this.tokenService.consumeRefreshToken(refreshToken).orElseThrow(InvalidRefreshTokenException::new);
	}

}
