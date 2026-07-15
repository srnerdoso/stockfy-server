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

package br.com.threadstech.stockfy.modules.users.domain.model;

import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTests {

	@Test
	@DisplayName("Should create active user by default")
	void shouldCreateActiveUserByDefault() {
		User user = createUser();
		assertThat(user.isActive()).isTrue();
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	@DisplayName("Should lock user account")
	void shouldLockUserAccount() {
		User user = createUser();
		user.lock();
		assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
	}

	@Test
	@DisplayName("Should unlock user account")
	void shouldUnlockUserAccount() {
		User user = createUser();
		user.lock();
		user.unlock();
		assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	@DisplayName("Should deactivate user (soft delete)")
	void shouldDeactivateUser() {
		User user = createUser();
		user.deactivate();
		assertThat(user.isActive()).isFalse();
	}

	private User createUser() {
		return User.builder()
			.id(UUID.randomUUID())
			.name("John Doe")
			.email(new Email("john@example.com"))
			.password(new Password("password123"))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
