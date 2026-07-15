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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateResetCodeUseCase {

	private final SecureRandom secureRandom = new SecureRandom();

	private final UserRepository userRepository;

	private final ResetCodeHasher resetCodeHasher;

	@Transactional
	public String execute(UUID userId) {
		User user = this.userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

		String code = String.format("%06d", this.secureRandom.nextInt(1000000));
		user.setResetPasswordCodeHash(this.resetCodeHasher.hash(code));
		user.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(24));

		this.userRepository.update(user);
		return code;
	}

}
