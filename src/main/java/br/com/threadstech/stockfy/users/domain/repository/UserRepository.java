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

package br.com.threadstech.stockfy.users.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {

	void save(User user);

	Optional<User> findById(UUID id);

	Optional<User> findByEmail(Email email);

	Optional<User> findByResetPasswordCodeHash(String resetPasswordCodeHash);

	List<User> findAll(String nameFilter);

	Page<User> findAll(String nameFilter, Pageable pageable);

	void update(User user);

	void deleteById(UUID id);

}
