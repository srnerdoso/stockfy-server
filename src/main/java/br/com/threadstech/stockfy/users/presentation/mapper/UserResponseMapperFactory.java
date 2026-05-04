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

package br.com.threadstech.stockfy.users.presentation.mapper;

import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.users.domain.model.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapperFactory {

	public UserResponse toResponse(User user) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		UUID currentUserId = (UUID) auth.getPrincipal();
		boolean isOwner = user.getId().equals(currentUserId);

		if (isAdmin) {
			return mapToFullResponse(user);
		}
		else if (isOwner) {
			return mapToOwnerResponse(user);
		}
		else {
			return mapToPublicResponse(user);
		}
	}

	private UserResponse mapToFullResponse(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail().value())
			.roles(user.getRoles())
			.status(user.getStatus())
			.active(user.isActive())
			// Fields like audit would need to be passed or fetched if Envers is used
			.build();
	}

	private UserResponse mapToOwnerResponse(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail().value())
			.roles(user.getRoles())
			.build();
	}

	private UserResponse mapToPublicResponse(User user) {
		return UserResponse.builder().id(user.getId()).name(user.getName()).build();
	}

}
