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

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.domain.model.Email;
import br.com.threadstech.stockfy.users.domain.model.Password;
import br.com.threadstech.stockfy.users.domain.model.User;
import br.com.threadstech.stockfy.users.domain.model.UserRole;
import br.com.threadstech.stockfy.users.domain.model.UserStatus;
import br.com.threadstech.stockfy.users.domain.repository.UserRepository;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import br.com.threadstech.stockfy.users.infrastructure.security.JwtService;
import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@SuppressWarnings({ "PMD.AvoidAccessibilityAlteration", "PMD.AvoidCatchingGenericException",
		"PMD.AvoidDuplicateLiterals", "PMD.AvoidLiteralsInIfCondition" })
class UserCreationIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDown() {
		try {
			var loginBucketsField = UserRateLimitConfig.class.getDeclaredField("loginBuckets");
			loginBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) loginBucketsField.get(this.rateLimitConfig)).clear();

			var generalBucketsField = UserRateLimitConfig.class.getDeclaredField("generalBuckets");
			generalBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) generalBucketsField.get(this.rateLimitConfig)).clear();
			this.rateLimitTimeMeter.reset();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	@DisplayName("Deve retornar 201 e criar usuário quando os dados forem válidos")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenDataIsValid_thenReturns201AndCreatesUser() throws Exception {
		String json = """
				{
				    "name": "John Doe",
				    "email": "created@example.com",
				    "password": "password123",
				    "role": "ADMIN"
				}
				""";

		this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isCreated())
			.andExpect(content().string(""));

		assertThat(this.userRepository.findByEmail(new Email("created@example.com")).isPresent()).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 201 e criar usuário quando token JWT pertencer a ADMIN")
	void registerUser_whenJwtTokenBelongsToAdmin_thenReturns201AndCreatesUser() throws Exception {
		User admin = createUser("Jwt Admin", "jwt-admin@example.com", UserRole.ADMIN);
		this.userRepository.save(admin);
		String accessToken = this.jwtService.generateToken(admin.getId(), admin.getRoles());
		String refreshToken = this.tokenService.generateRefreshToken(admin.getId());
		String json = """
				{
				    "name": "Jwt Created",
				    "email": "jwt-created@example.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		this.mockMvc
			.perform(post("/api/v1/users")
				.cookie(new Cookie("access_token", accessToken), new Cookie("refresh_token", refreshToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(content().string(""));

		assertThat(this.userRepository.findByEmail(new Email("jwt-created@example.com")).isPresent()).isTrue();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 401 quando refresh token for inválido")
	void registerUser_whenAccessTokenIsValidAndRefreshTokenIsInvalid_thenReturns401() throws Exception {
		User admin = createUser("Invalid Session Admin", "invalid-session-admin@example.com", UserRole.ADMIN);
		this.userRepository.save(admin);
		String accessToken = this.jwtService.generateToken(admin.getId(), admin.getRoles());
		String json = """
				{
				    "name": "Invalid Session Created",
				    "email": "invalid-session-created@example.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		this.mockMvc
			.perform(post("/api/v1/users")
				.cookie(new Cookie("access_token", accessToken), new Cookie("refresh_token", "invalid-refresh-token"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertThat(this.userRepository.findByEmail(new Email("invalid-session-created@example.com")).isPresent())
			.isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 401 quando não houver token de autenticação")
	void registerUser_whenNotAuthenticated_thenReturns401() throws Exception {
		String json = """
				{
				    "name": "John Doe",
				    "email": "unauth@example.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect((result) -> assertThat(result.getResponse().isCommitted()).isFalse());

		assertThat(this.userRepository.findByEmail(new Email("unauth@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 403 quando o usuário autenticado for um USER")
	@WithMockUser(roles = "USER")
	void registerUser_whenUserRole_thenReturns403() throws Exception {
		String json = """
				{
				    "name": "John Doe",
				    "email": "user@example.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""))
			.andExpect((result) -> assertThat(result.getResponse().isCommitted()).isFalse());

		assertThat(this.userRepository.findByEmail(new Email("user@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando o nome for inválido")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenNameIsInvalid_thenReturns400() throws Exception {
		String jsonNull = "{ \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"USER\" }";

		var result = this.mockMvc
			.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull));
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(result, "name", "O nome é obrigatório.");

		String jsonInvalid = "{ \"name\": \"User'); DROP TABLE users; --\", \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"USER\" }";

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc
					.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)),
				"name", "O nome contém caracteres inválidos.");
		assertThat(this.userRepository.findByEmail(new Email("john@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando o email for inválido")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenEmailIsInvalid_thenReturns400() throws Exception {
		String jsonNull = "{ \"name\": \"John\", \"password\": \"password123\", \"role\": \"USER\" }";
		String jsonInvalid = "{ \"name\": \"John\", \"email\": \"invalid-email\", \"password\": \"password123\", \"role\": \"USER\" }";

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull)),
				"email", "O e-mail é obrigatório.");
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc
					.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)),
				"email", "O e-mail informado é inválido.");
		assertThat(this.userRepository.findByEmail(new Email("john@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando a senha for inválida")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenPasswordIsInvalid_thenReturns400() throws Exception {
		String jsonNull = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"role\": \"USER\" }";
		String jsonBlank = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \" \", \"role\": \"USER\" }";

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonNull)),
				"password", "A senha é obrigatória.");
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonBlank)),
				"password", "A senha é obrigatória.");
		assertThat(this.userRepository.findByEmail(new Email("john@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando a role for inválida")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenRoleIsMissingOrInvalid_thenReturns400() throws Exception {
		String jsonMissing = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \"password123\" }";
		String jsonInvalid = "{ \"name\": \"John\", \"email\": \"john@example.com\", \"password\": \"password123\", \"role\": \"INVALID\" }";

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc
					.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonMissing)),
				"role", "O perfil do usuário é obrigatório.");
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(
				this.mockMvc
					.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(jsonInvalid)),
				"role", "não deve ser nulo");
		assertThat(this.userRepository.findByEmail(new Email("john@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 422 quando as senhas não coincidem")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenPasswordsDoNotMatch_thenReturns422() throws Exception {
		String json = """
				{
				    "name": "John Doe",
				    "email": "test@example.com",
				    "password": "password123",
				    "confirmPassword": "differentPassword",
				    "role": "USER"
				}
				""";

		var result = this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json));

		result.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("As senhas não coincidem."))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("As senhas não coincidem."));

		assertThat(this.userRepository.findByEmail(new Email("test@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Deve retornar 400 ou 422 para SQL Injection (tratado como validação inválida)")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenInputContainsSqlInjection_thenReturnsBadRequest() throws Exception {
		String json = """
				{
				    "name": "User'); DROP TABLE users; --",
				    "email": "sql@example.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite de 10 requisições por minuto")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenLimitExceeded_thenReturns429() throws Exception {
		String json = """
				{
				    "name": "User",
				    "email": "user@test.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		for (int i = 0; i <= 11; i++) {
			var result = this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
				.content(json.replace("user@test.com", "user" + i + "@test.com")));

			if (i > 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}
	}

	@Test
	@DisplayName("Deve permitir requisições após o período de janela de 1 minuto")
	@WithMockUser(roles = "ADMIN")
	void registerUser_whenWindowPasses_thenReturns201() throws Exception {
		String json = """
				{
				    "name": "User",
				    "email": "user@test.com",
				    "password": "password123",
				    "role": "USER"
				}
				""";

		for (int i = 0; i < 11; i++) {
			this.mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
				.content(json.replace("user@test.com", "user" + i + "@test.com")));
		}

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		this.mockMvc
			.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
				.content(json.replace("user@test.com", "after-window@test.com")))
			.andExpect(status().isCreated());
	}

	private User createUser(String name, String email, UserRole role) {
		return User.builder()
			.id(UUID.randomUUID())
			.name(name)
			.email(new Email(email))
			.password(new Password("password123"))
			.roles(Set.of(role))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
