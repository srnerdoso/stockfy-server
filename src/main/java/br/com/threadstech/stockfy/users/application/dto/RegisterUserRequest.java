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

import br.com.threadstech.stockfy.users.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterUserRequest(
		@NotBlank(message = "{user.name.not-blank}") @Pattern(regexp = "^[\\p{L}\\p{M}0-9 .'-]+$",
				message = "{user.name.pattern}") String name,
		@NotBlank(message = "{user.email.not-blank}") @Email(message = "{user.email.email}") String email,
		@NotBlank(message = "{user.password.not-blank}") String password, String confirmPassword,
		@NotNull(message = "{user.role.not-null}") UserRole role) {
}
