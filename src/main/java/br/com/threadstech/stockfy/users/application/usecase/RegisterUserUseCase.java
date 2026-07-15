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

import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.RegisterUserRequest;
import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	public void execute(RegisterUserRequest request) {
		if (request.confirmPassword() != null && !request.password().equals(request.confirmPassword())) {
			throw new PasswordMismatchException();
		}
		Email userEmail = new Email(request.email());
		if (this.userRepository.findByEmail(userEmail).isPresent()) {
			throw new EmailAlreadyExistsException();
		}

		User user = User.builder()
			.id(UUID.randomUUID())
			.name(request.name())
			.email(userEmail)
			.password(new Password(this.passwordEncoder.encode(request.password())))
			.roles(Set.of(request.role()))
			.build();

		this.userRepository.save(user);
	}

}
