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

package br.com.threadstech.stockfy.modules.users.presentation.mapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.presentation.mapper.UserResponseMapper;
import br.com.threadstech.stockfy.users.presentation.mapper.UserResponseMapperFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserResponseMapperFactoryTests {

	private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

	private static final String USER_NAME = "John";

	private final User user = User.builder()
		.id(USER_ID)
		.name(USER_NAME)
		.email(new Email("john@example.com"))
		.roles(Set.of(UserRole.USER))
		.build();

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Deve delegar mapeamento completo para MapStruct quando usuario autenticado for ADMIN")
	void toResponse_whenCurrentUserIsAdmin_thenDelegatesFullMappingToMapStruct() {
		UserResponseMapper mapper = mock(UserResponseMapper.class);
		UserResponse expected = UserResponse.builder()
			.id(USER_ID)
			.name(USER_NAME)
			.email("john@example.com")
			.roles(Set.of(UserRole.USER))
			.build();
		given(mapper.toFullResponse(this.user)).willReturn(expected);
		SecurityContextHolder.getContext().setAuthentication(adminAuthentication());

		UserResponse response = new UserResponseMapperFactory(mapper).toResponse(this.user);

		assertThat(response).isEqualTo(expected);
		verify(mapper).toFullResponse(this.user);
		verify(mapper, never()).toOwnerResponse(any());
		verify(mapper, never()).toPublicResponse(any());
	}

	@Test
	@DisplayName("Deve delegar mapeamento para owner quando usuario autenticado for dono")
	void toResponse_whenCurrentUserIsOwner_thenDelegatesOwnerMappingToMapStruct() {
		UserResponseMapper mapper = mock(UserResponseMapper.class);
		UserResponse expected = UserResponse.builder()
			.id(USER_ID)
			.name(USER_NAME)
			.email("john@example.com")
			.roles(Set.of(UserRole.USER))
			.build();
		given(mapper.toOwnerResponse(this.user)).willReturn(expected);
		SecurityContextHolder.getContext().setAuthentication(ownerAuthentication());

		UserResponse response = new UserResponseMapperFactory(mapper).toResponse(this.user);

		assertThat(response).isEqualTo(expected);
		verify(mapper).toOwnerResponse(this.user);
		verify(mapper, never()).toFullResponse(any());
		verify(mapper, never()).toPublicResponse(any());
	}

	@Test
	@DisplayName("Deve delegar mapeamento publico quando usuario autenticado nao for admin nem dono")
	void toResponse_whenCurrentUserIsNotAdminAndNotOwner_thenDelegatesPublicMappingToMapStruct() {
		UserResponseMapper mapper = mock(UserResponseMapper.class);
		UserResponse expected = UserResponse.builder().id(USER_ID).name(USER_NAME).roles(Set.of()).build();
		given(mapper.toPublicResponse(this.user)).willReturn(expected);
		SecurityContextHolder.getContext().setAuthentication(otherUserAuthentication());

		UserResponse response = new UserResponseMapperFactory(mapper).toResponse(this.user);

		assertThat(response).isEqualTo(expected);
		verify(mapper).toPublicResponse(this.user);
		verify(mapper, never()).toFullResponse(any());
		verify(mapper, never()).toOwnerResponse(any());
	}

	private UsernamePasswordAuthenticationToken adminAuthentication() {
		return new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null,
				List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}

	private UsernamePasswordAuthenticationToken ownerAuthentication() {
		return new UsernamePasswordAuthenticationToken(USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}

	private UsernamePasswordAuthenticationToken otherUserAuthentication() {
		return new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
	}

}
