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

package br.com.threadstech.stockfy.users.domain.model;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.domain.exception.InvalidUserRolesException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

	private UUID id;

	private String name;

	private Email email;

	private Password password;

	@Builder.Default
	private Set<UserRole> roles = EnumSet.of(UserRole.USER);

	@Builder.Default
	private UserStatus status = UserStatus.ACTIVE;

	@Builder.Default
	private boolean active = true;

	private String resetPasswordCodeHash;

	private LocalDateTime resetPasswordExpiresAt;

	private LocalDateTime createdAt;

	private UUID createdBy;

	private LocalDateTime updatedAt;

	private UUID updatedBy;

	public void lock() {
		this.status = UserStatus.LOCKED;
	}

	public void unlock() {
		this.status = UserStatus.ACTIVE;
	}

	public void deactivate() {
		this.active = false;
	}

	public void activate() {
		this.active = true;
	}

	public void updateRoles(Set<UserRole> rolesToAdd, Set<UserRole> rolesToRemove) {
		EnumSet<UserRole> updatedRoles = EnumSet.copyOf(this.roles);
		updatedRoles.removeAll(rolesToRemove);
		updatedRoles.addAll(rolesToAdd);

		if (updatedRoles.isEmpty()) {
			throw new InvalidUserRolesException();
		}

		this.roles = updatedRoles;
	}

}
