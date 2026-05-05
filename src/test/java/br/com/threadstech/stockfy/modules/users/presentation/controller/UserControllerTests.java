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

package br.com.threadstech.stockfy.modules.users.presentation.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserRepository userRepository;

	private final UUID userId = UUID.randomUUID();

	@Test
	@DisplayName("Should return user profile when authenticated")
	void shouldReturnUserProfile() throws Exception {
		User user = User.builder()
			.id(this.userId)
			.name("John Doe")
			.email(new Email("john@example.com"))
			.roles(Set.of(UserRole.USER))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();

		given(this.userRepository.findById(this.userId)).willReturn(Optional.of(user));

		// Manually set security context for test
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(this.userId, null,
					List.of(new SimpleGrantedAuthority("ROLE_USER"))));

		this.mockMvc.perform(get("/api/v1/users/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("John Doe"))
			.andExpect(jsonPath("$.email").value("john@example.com"))
			.andExpect(jsonPath("$.roles").isArray())
			.andExpect(jsonPath("$.roles[0]").value("USER"));
	}

}
