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

import java.util.Set;

import br.com.threadstech.stockfy.users.application.dto.UserResponse;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {

	String NAME_FIELD = "name";

	String ID_FIELD = "id";

	String ROLES_FIELD = "roles";

	@Mapping(target = "email", source = "email.value")
	UserResponse toFullResponse(User user);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = ID_FIELD, source = ID_FIELD)
	@Mapping(target = NAME_FIELD, source = NAME_FIELD)
	@Mapping(target = "email", source = "email.value")
	@Mapping(target = ROLES_FIELD, source = ROLES_FIELD)
	UserResponse toOwnerResponse(User user);

	@BeanMapping(ignoreByDefault = true)
	@Mapping(target = ID_FIELD, source = ID_FIELD)
	@Mapping(target = NAME_FIELD, source = NAME_FIELD)
	@Mapping(target = ROLES_FIELD, expression = "java(emptyRoles())")
	UserResponse toPublicResponse(User user);

	default Set<UserRole> emptyRoles() {
		return Set.of();
	}

}
