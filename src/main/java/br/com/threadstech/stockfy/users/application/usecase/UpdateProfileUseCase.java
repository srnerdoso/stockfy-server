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

import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProfileUseCase {

	private final UserRepository userRepository;

	@Transactional
	public void execute(UUID userId, String name, String email) {
		User user = this.userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
		Email newEmail = resolveNewEmail(user, email);

		if (name == null && newEmail == null) {
			return;
		}

		if (name != null) {
			user.setName(name);
		}

		if (newEmail != null) {
			user.setEmail(newEmail);
		}

		this.userRepository.update(user);
	}

	private Email resolveNewEmail(User user, String email) {
		if (email == null) {
			return null;
		}

		Email newEmail = new Email(email);
		return newEmail.equals(user.getEmail()) ? null : newEmail;
	}

}
