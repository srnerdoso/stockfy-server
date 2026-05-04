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

package br.com.threadstech.stockfy.users.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

	private final SpringDataUserRepository repository;

	private final UserPersistenceMapper mapper;

	@Override
	public void save(User user) {
		repository.save(mapper.toEntity(user));
	}

	@Override
	public Optional<User> findById(UUID id) {
		return repository.findById(id).map(mapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(Email email) {
		return repository.findByEmail(email.value()).map(mapper::toDomain);
	}

	@Override
	public Optional<User> findByResetPasswordCodeHash(String resetPasswordCodeHash) {
		return repository.findByResetPasswordCodeHash(resetPasswordCodeHash).map(mapper::toDomain);
	}

	@Override
	public List<User> findAll(String nameFilter) {
		return repository.findAllByName(nameFilter).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Page<User> findAll(String nameFilter, Pageable pageable) {
		if (nameFilter == null) {
			return repository.findAll(pageable).map(mapper::toDomain);
		}
		return repository.findByNameContainingIgnoreCase(nameFilter, pageable).map(mapper::toDomain);
	}

	@Override
	public void update(User user) {
		repository.save(mapper.toEntity(user));
	}

	@Override
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}

}
