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

package br.com.threadstech.stockfy.users.application.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserListItemResponse(String name, String email, Set<UserRole> roles, UserStatus status,
		LocalDateTime createdAt, UUID createdBy, LocalDateTime updatedAt, UUID updatedBy) {

	public static UserListItemResponse from(User user, UserListType type) {
		return switch (type) {
			case SUMMARY -> summary(user);
			case DETAILED -> detailed(user);
		};
	}

	private static UserListItemResponse summary(User user) {
		return new UserListItemResponse(user.getName(), user.getEmail().value(), user.getRoles(), null, null, null,
				null, null);
	}

	private static UserListItemResponse detailed(User user) {
		return new UserListItemResponse(user.getName(), user.getEmail().value(), user.getRoles(), user.getStatus(),
				user.getCreatedAt(), user.getCreatedBy(), user.getUpdatedAt(), user.getUpdatedBy());
	}
}
