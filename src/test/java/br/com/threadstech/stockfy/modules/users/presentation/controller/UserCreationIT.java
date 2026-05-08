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

import br.com.threadstech.stockfy.ContainersConfiguration;
import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitBucketCleaner;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ ContainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserCreationIT {

	private static final int GENERAL_RATE_LIMIT = 10;

	private static final int JSON_OBJECT_START_LENGTH = 1;

	private static final String USERS_ENDPOINT = "/api/v1/users";

	private static final String ACCESS_TOKEN_COOKIE = "access_token";

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private static final String DEFAULT_PASSWORD = "password123";

	private static final String USER_ROLE = "USER";

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String DEFAULT_NAME = "John";

	private static final String DEFAULT_FULL_NAME = "John Doe";

	private static final String JOHN_EMAIL = "john@example.com";

	private static final String RATE_LIMIT_USER_EMAIL = "user@test.com";

	private static final String NAME_FIELD = "name";

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
	void tearDownRateLimit() {
		RateLimitBucketCleaner.clearAll(this.rateLimitConfig, this.rateLimitTimeMeter);
	}

	@Test
	@DisplayName("Deve retornar 201 e criar usuário quando os dados forem válidos")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenDataIsValid_thenReturns201AndCreatesUser() throws Exception {
		String json = userBody(DEFAULT_FULL_NAME, "created@example.com", DEFAULT_PASSWORD, null, ADMIN_ROLE);

		registerUserRequest(json).andExpect(status().isCreated()).andExpect(content().string(""));

		assertThat(this.userRepository.findByEmail(new Email("created@example.com")).isPresent()).isTrue();
	}

	@Test
	@DisplayName("Deve retornar 201 e criar usuário quando token JWT pertencer a ADMIN")
	void registerUser_whenJwtTokenBelongsToAdmin_thenReturns201AndCreatesUser() throws Exception {
		User admin = createUser("Jwt Admin", "jwt-admin@example.com", UserRole.ADMIN);
		this.userRepository.save(admin);
		String accessToken = this.jwtService.generateToken(admin.getId(), admin.getRoles());
		String refreshToken = this.tokenService.generateRefreshToken(admin.getId());
		String json = userBody("Jwt Created", "jwt-created@example.com", DEFAULT_PASSWORD, null, USER_ROLE);

		this.mockMvc
			.perform(post(USERS_ENDPOINT)
				.cookie(new Cookie(ACCESS_TOKEN_COOKIE, accessToken), new Cookie(REFRESH_TOKEN_COOKIE, refreshToken))
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
		String json = userBody("Invalid Session Created", "invalid-session-created@example.com", DEFAULT_PASSWORD, null,
				USER_ROLE);

		this.mockMvc
			.perform(post(USERS_ENDPOINT)
				.cookie(new Cookie(ACCESS_TOKEN_COOKIE, accessToken),
						new Cookie(REFRESH_TOKEN_COOKIE, "invalid-refresh-token"))
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
		String json = userBody(DEFAULT_FULL_NAME, "unauth@example.com", DEFAULT_PASSWORD, null, USER_ROLE);

		registerUserRequest(json).andExpect(status().isUnauthorized())
			.andExpect(content().string(""))
			.andExpect((result) -> assertThat(result.getResponse().isCommitted()).isFalse());

		assertThat(this.userRepository.findByEmail(new Email("unauth@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 403 quando o usuário autenticado for um USER")
	@WithMockUser(roles = USER_ROLE)
	void registerUser_whenUserRole_thenReturns403() throws Exception {
		String json = userBody(DEFAULT_FULL_NAME, "user@example.com", DEFAULT_PASSWORD, null, USER_ROLE);

		registerUserRequest(json).andExpect(status().isForbidden())
			.andExpect(content().string(""))
			.andExpect((result) -> assertThat(result.getResponse().isCommitted()).isFalse());

		assertThat(this.userRepository.findByEmail(new Email("user@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando o nome for inválido")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenNameIsInvalid_thenReturns400() throws Exception {
		String jsonNull = userBody(null, JOHN_EMAIL, DEFAULT_PASSWORD, null, USER_ROLE);
		var result = registerUserRequest(jsonNull);
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(result, NAME_FIELD, "O nome é obrigatório.");

		String jsonInvalid = userBody("User'); DROP TABLE users; --", JOHN_EMAIL, DEFAULT_PASSWORD, null, USER_ROLE);
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonInvalid), NAME_FIELD,
				"O nome contém caracteres inválidos.");
		assertThat(this.userRepository.findByEmail(new Email(JOHN_EMAIL)).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando o email for inválido")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenEmailIsInvalid_thenReturns400() throws Exception {
		String jsonNull = userBody(DEFAULT_NAME, null, DEFAULT_PASSWORD, null, USER_ROLE);
		String jsonInvalid = userBody(DEFAULT_NAME, "invalid-email", DEFAULT_PASSWORD, null, USER_ROLE);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonNull), "email",
				"O e-mail é obrigatório.");
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonInvalid), "email",
				"O e-mail informado é inválido.");
		assertThat(this.userRepository.findByEmail(new Email(JOHN_EMAIL)).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando a senha for inválida")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenPasswordIsInvalid_thenReturns400() throws Exception {
		String jsonNull = userBody(DEFAULT_NAME, JOHN_EMAIL, null, null, USER_ROLE);
		String jsonBlank = userBody(DEFAULT_NAME, JOHN_EMAIL, " ", null, USER_ROLE);

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonNull), "password",
				"A senha é obrigatória.");
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonBlank), "password",
				"A senha é obrigatória.");
		assertThat(this.userRepository.findByEmail(new Email(JOHN_EMAIL)).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 400 quando a role for inválida")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenRoleIsMissingOrInvalid_thenReturns400() throws Exception {
		String jsonMissing = userBody(DEFAULT_NAME, JOHN_EMAIL, DEFAULT_PASSWORD, null, null);
		String jsonInvalid = userBody(DEFAULT_NAME, JOHN_EMAIL, DEFAULT_PASSWORD, null, "INVALID");

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonMissing), "role",
				"O perfil do usuário é obrigatório.");
		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(jsonInvalid), "role",
				"não deve ser nulo");
		assertThat(this.userRepository.findByEmail(new Email(JOHN_EMAIL)).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Não deve persistir usuário e deve retornar 422 quando as senhas não coincidem")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenPasswordsDoNotMatch_thenReturns422() throws Exception {
		String json = userBody(DEFAULT_FULL_NAME, "test@example.com", DEFAULT_PASSWORD, "differentPassword", USER_ROLE);

		var result = registerUserRequest(json);

		result.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Unprocessable Entity"))
			.andExpect(jsonPath("$.status").value(422))
			.andExpect(jsonPath("$.detail").value("As senhas não coincidem."))
			.andExpect(jsonPath("$.instance").value(USERS_ENDPOINT))
			.andExpect(jsonPath("$.fieldErrors.length()").value(1))
			.andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"))
			.andExpect(jsonPath("$.fieldErrors[0].message").value("As senhas não coincidem."))
			.andExpect(jsonPath("$.fieldErrors[1]").doesNotExist());

		assertThat(this.userRepository.findByEmail(new Email("test@example.com")).isPresent()).isFalse();
	}

	@Test
	@DisplayName("Deve retornar 400 ou 422 para SQL Injection (tratado como validação inválida)")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenInputContainsSqlInjection_thenReturnsBadRequest() throws Exception {
		String email = "sql@example.com";
		String json = userBody("User'); DROP TABLE users; --", email, DEFAULT_PASSWORD, null, USER_ROLE);
		boolean userExistsBeforeRequest = this.userRepository.findByEmail(new Email(email)).isPresent();

		ApiErrorResponseAssertions.assertBadRequestFieldValidation(registerUserRequest(json), NAME_FIELD,
				"O nome contém caracteres inválidos.");
		assertThat(this.userRepository.findByEmail(new Email(email)).isPresent()).isEqualTo(userExistsBeforeRequest);
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite de 10 requisições por minuto")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenLimitExceeded_thenReturns429() throws Exception {
		String json = userBody("User", RATE_LIMIT_USER_EMAIL, DEFAULT_PASSWORD, null, USER_ROLE);

		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			registerUserRequest(json.replace(RATE_LIMIT_USER_EMAIL, "user" + i + "@test.com"))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));
		}
		registerUserRequest(json.replace(RATE_LIMIT_USER_EMAIL, "blocked-after-threshold@test.com"))
			.andExpect(status().isTooManyRequests());
	}

	@Test
	@DisplayName("Deve permitir requisições após o período de janela de 1 minuto")
	@WithMockUser(roles = ADMIN_ROLE)
	void registerUser_whenWindowPasses_thenReturns201() throws Exception {
		String json = userBody("User", RATE_LIMIT_USER_EMAIL, DEFAULT_PASSWORD, null, USER_ROLE);

		for (int i = 0; i < GENERAL_RATE_LIMIT; i++) {
			registerUserRequest(json.replace(RATE_LIMIT_USER_EMAIL, "user" + i + "@test.com"))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));
		}
		registerUserRequest(json.replace(RATE_LIMIT_USER_EMAIL, "blocked-before-reset@test.com"))
			.andExpect(status().isTooManyRequests());

		this.rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		registerUserRequest(json.replace(RATE_LIMIT_USER_EMAIL, "after-window@test.com"))
			.andExpect(status().isCreated());
	}

	private ResultActions registerUserRequest(String body) throws Exception {
		return this.mockMvc.perform(post(USERS_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private String userBody(String name, String email, String password, String confirmPassword, String role) {
		StringBuilder body = new StringBuilder("{");
		appendJsonField(body, NAME_FIELD, name);
		appendJsonField(body, "email", email);
		appendJsonField(body, "password", password);
		appendJsonField(body, "confirmPassword", confirmPassword);
		appendJsonField(body, "role", role);
		body.append("}");
		return body.toString();
	}

	private void appendJsonField(StringBuilder body, String fieldName, String value) {
		if (value == null) {
			return;
		}
		if (body.length() > JSON_OBJECT_START_LENGTH) {
			body.append(",");
		}
		body.append("\"").append(fieldName).append("\":\"").append(value).append("\"");
	}

	private User createUser(String name, String email, UserRole role) {
		return User.builder()
			.id(UUID.randomUUID())
			.name(name)
			.email(new Email(email))
			.password(new Password(DEFAULT_PASSWORD))
			.roles(Set.of(role))
			.status(UserStatus.ACTIVE)
			.active(true)
			.build();
	}

}
