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

import java.util.List;

import br.com.threadstech.stockfy.users.application.dto.FindAllUsersResponse;
import br.com.threadstech.stockfy.users.application.dto.UserListItemResponse;
import br.com.threadstech.stockfy.users.application.dto.UserListType;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FindAllUsersUseCase {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public FindAllUsersResponse<UserListItemResponse> execute(String name, UserListType type, Pageable pageable) {
		Page<User> users = userRepository.findAll(normalizeName(name), pageable);
		List<UserListItemResponse> content = users.getContent()
			.stream()
			.map(user -> UserListItemResponse.from(user, type))
			.toList();

		return new FindAllUsersResponse<>(content, users.getNumber(), users.getSize(), users.getTotalElements(),
				users.getTotalPages());
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		return name.trim();
	}

}
