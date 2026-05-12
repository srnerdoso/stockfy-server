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

import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
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
		try {
			this.repository.saveAndFlush(this.mapper.toEntity(user));
		}
		catch (DataIntegrityViolationException ex) {
			throw translateConstraintViolation(ex);
		}
	}

	@Override
	public Optional<User> findById(UUID id) {
		return this.repository.findById(id).map(this.mapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(Email email) {
		return this.repository.findByEmail(email.value()).map(this.mapper::toDomain);
	}

	@Override
	public Optional<User> findByResetPasswordCodeHash(String resetPasswordCodeHash) {
		return this.repository.findByResetPasswordCodeHash(resetPasswordCodeHash).map(this.mapper::toDomain);
	}

	@Override
	public List<User> findAll(String nameFilter) {
		return this.repository.findAllByName(nameFilter).stream().map(this.mapper::toDomain).toList();
	}

	@Override
	public Page<User> findAll(String nameFilter, Pageable pageable) {
		if (nameFilter == null) {
			return this.repository.findAll(pageable).map(this.mapper::toDomain);
		}
		return this.repository.findByNameContainingIgnoreCase(nameFilter, pageable).map(this.mapper::toDomain);
	}

	@Override
	public void update(User user) {
		try {
			this.repository.saveAndFlush(this.mapper.toEntity(user));
		}
		catch (DataIntegrityViolationException ex) {
			throw translateConstraintViolation(ex);
		}
	}

	@Override
	public void deleteById(UUID id) {
		this.repository.deleteById(id);
		this.repository.flush();
	}

	private RuntimeException translateConstraintViolation(DataIntegrityViolationException ex) {
		String message = String.valueOf(ex.getMostSpecificCause().getMessage());
		if (message.contains("users_email_key") || message.contains("uk_users_email")
				|| message.contains("users_email_unique")) {
			return new EmailAlreadyExistsException();
		}
		return ex;
	}

}
