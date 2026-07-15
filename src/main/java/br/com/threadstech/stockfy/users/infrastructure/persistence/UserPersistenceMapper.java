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

import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

	@Mapping(target = "passwordHash", source = "password.value")
	@Mapping(target = "email", source = "email.value")
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	UserJpaEntity toEntity(User user);

	@Mapping(target = "password", source = "passwordHash", qualifiedByName = "toPassword")
	@Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
	User toDomain(UserJpaEntity entity);

	@Named("toEmail")
	default Email toEmail(String value) {
		return new Email(value);
	}

	@Named("toPassword")
	default Password toPassword(String value) {
		// Here we wrap the hash in the Password VO.
		// Note: Password VO validation might need to be relaxed for hashes or we use a
		// specific
		// constructor/factory.
		// For now, let's assume the hash string passes the 8 char minimum validation.
		return new Password(value);
	}

}
