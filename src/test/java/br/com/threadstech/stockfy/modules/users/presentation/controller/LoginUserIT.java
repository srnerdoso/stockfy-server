package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions.assertBadRequestFieldValidation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LoginUserIT {

	private static final String USER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String USER_EMAIL = "bruno.user@example.com";

	private static final String RAW_PASSWORD = "password123";

	private static final String WRONG_PASSWORD = "wrongPassword123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@BeforeEach
	void setPassword() {
		jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?::uuid",
				passwordEncoder.encode(RAW_PASSWORD), USER_ID);
	}

	@AfterEach
	void tearDown() {
		clearRateLimitBuckets();
		redisTemplate.delete("login_attempts:" + USER_EMAIL);
		rateLimitTimeMeter.reset();
	}

	@Test
	@DisplayName("Deve retornar 200 com cookies seguros e HttpOnly quando credenciais forem validas")
	void loginUser_whenCredentialsAreValid_thenReturns200WithSecureHttpOnlyCookies() throws Exception {
		long usersBeforeRequest = countUsers();

		var result = mockMvc
			.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON).content(validBody()))
			.andExpect(status().isOk())
			.andExpect(content().string(""))
			.andExpect(cookie().exists("access_token"))
			.andExpect(cookie().exists("refresh_token"))
			.andExpect(cookie().httpOnly("access_token", true))
			.andExpect(cookie().httpOnly("refresh_token", true))
			.andReturn();

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
		assertThat(result.getResponse().getHeaders("Set-Cookie").get(0), containsString("Secure"));
		assertThat(result.getResponse().getHeaders("Set-Cookie").get(1), containsString("Secure"));
		Cookie refreshToken = result.getResponse().getCookie("refresh_token");
		assertNotNull(refreshToken);
		assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + refreshToken.getValue())));
		assertNull(redisTemplate.opsForValue().get("login_attempts:" + USER_EMAIL));
	}

	@Test
	@DisplayName("Deve retornar 400 quando email for omitido")
	void loginUser_whenEmailIsMissing_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
					.content("{\"password\":\"password123\"}")),
				"email", "O e-mail é obrigatório.");

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 400 quando email for invalido")
	void loginUser_whenEmailIsInvalid_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"invalid-email\",\"password\":\"password123\"}")),
				"email", "O e-mail informado é inválido.");

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 400 com todos os erros quando email e senha forem omitidos")
	void loginUser_whenEmailAndPasswordAreMissing_thenReturns400WithBothFieldErrors() throws Exception {
		mockMvc.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Validation Error"))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.detail").value("Erro de validação nos campos informados."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors.length()").value(2))
			.andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("email")))
			.andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("password")))
			.andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("O e-mail é obrigatório.")))
			.andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("A senha é obrigatória.")));
	}

	@Test
	@DisplayName("Deve retornar 400 quando senha for omitida")
	void loginUser_whenPasswordIsMissing_thenReturns400() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"bruno.user@example.com\"}")),
				"password", "A senha é obrigatória.");

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 400 quando email contiver SQL Injection")
	void loginUser_whenEmailContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"user@example.com' OR '1'='1\",\"password\":\"password123\"}")),
				"email", "O e-mail informado é inválido.");

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 401 sem dados sensiveis quando senha estiver incorreta")
	void loginUser_whenPasswordIsWrong_thenReturns401WithoutSensitiveData() throws Exception {
		long usersBeforeRequest = countUsers();

		var result = mockMvc
			.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"bruno.user@example.com\",\"password\":\"wrongPassword123\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		String response = result.getResponse().getContentAsString();
		assertThat(response, not(containsString(WRONG_PASSWORD)));
		assertThat(response, not(containsString("password-hash")));
		assertThat(response, not(containsString("access_token")));
		assertThat(response, not(containsString("refresh_token")));
		assertThat(response, not(containsString("RuntimeException")));
		assertThat(response, not(containsString(".java:")));
		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 401 sem dados sensiveis quando email nao existir")
	void loginUser_whenEmailDoesNotExist_thenReturns401WithoutSensitiveData() throws Exception {
		long usersBeforeRequest = countUsers();

		var result = mockMvc
			.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"missing.user@example.com\",\"password\":\"password123\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(cookie().doesNotExist("access_token"))
			.andExpect(cookie().doesNotExist("refresh_token"))
			.andReturn();

		String response = result.getResponse().getContentAsString();
		assertThat(response, not(containsString("missing.user@example.com")));
		assertThat(response, not(containsString("password123")));
		assertThat(response, not(containsString("password-hash")));
		assertThat(response, not(containsString("access_token")));
		assertThat(response, not(containsString("refresh_token")));
		assertThat(response, not(containsString("RuntimeException")));
		assertThat(response, not(containsString(".java:")));
		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 401 quando senha contiver SQL Injection")
	void loginUser_whenPasswordContainsSqlInjection_thenReturns401AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();
		String sqlInjectionPassword = "' OR '1'='1";

		var result = mockMvc
			.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"bruno.user@example.com\",\"password\":\"" + sqlInjectionPassword + "\"}"))
			.andExpect(status().isUnauthorized())
			.andReturn();

		assertThat(result.getResponse().getContentAsString(), not(containsString(sqlInjectionPassword)));
		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve bloquear usuario na decima quinta tentativa invalida")
	void loginUser_whenFifteenthInvalidAttempt_thenLocksUser() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < 5; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist("access_token"))
				.andExpect(cookie().doesNotExist("refresh_token"));
		}
		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));
		for (int i = 0; i < 5; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist("access_token"))
				.andExpect(cookie().doesNotExist("refresh_token"));
		}
		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));
		for (int i = 0; i < 5; i++) {
			performWrongPasswordLogin().andExpect(status().isUnauthorized())
				.andExpect(cookie().doesNotExist("access_token"))
				.andExpect(cookie().doesNotExist("refresh_token"));
		}

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("LOCKED", userStatus());
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite de login")
	void loginUser_whenLimitExceeded_thenReturns429() throws Exception {
		for (int i = 0; i <= 5; i++) {
			var result = performWrongPasswordLogin();
			if (i == 5) {
				result.andExpect(status().isTooManyRequests())
					.andExpect(cookie().doesNotExist("access_token"))
					.andExpect(cookie().doesNotExist("refresh_token"));
			}
		}

		assertEquals("ACTIVE", userStatus());
	}

	@Test
	@DisplayName("Deve processar login quando janela de rate limit expirar")
	void loginUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		for (int i = 0; i < 5; i++) {
			performWrongPasswordLogin();
		}

		performWrongPasswordLogin().andExpect(status().isTooManyRequests());

		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		mockMvc.perform(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON).content(validBody()))
			.andExpect(status().isOk())
			.andExpect(content().string(""));
	}

	@Test
	@DisplayName("Nao deve compartilhar rate limit entre IPs diferentes")
	void loginUser_whenDifferentClientIp_thenDoesNotShareRateLimitBucket() throws Exception {
		for (int i = 0; i < 5; i++) {
			mockMvc.perform(wrongPasswordRequest().with(remoteAddress("10.0.0.1")));
		}

		mockMvc.perform(wrongPasswordRequest().with(remoteAddress("10.0.0.2"))).andExpect(status().isUnauthorized());
	}

	private org.springframework.test.web.servlet.ResultActions performWrongPasswordLogin() throws Exception {
		return mockMvc.perform(wrongPasswordRequest());
	}

	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder wrongPasswordRequest() {
		return post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"bruno.user@example.com\",\"password\":\"wrongPassword123\"}");
	}

	private RequestPostProcessor remoteAddress(String remoteAddress) {
		return request -> {
			request.setRemoteAddr(remoteAddress);
			return request;
		};
	}

	private String validBody() {
		return "{\"email\":\"bruno.user@example.com\",\"password\":\"password123\"}";
	}

	private long countUsers() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return count == null ? 0 : count;
	}

	private String userStatus() {
		return jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, USER_ID);
	}

	private void clearRateLimitBuckets() {
		try {
			clearBucketMap("loginBuckets");
			clearBucketMap("generalBuckets");
			clearBucketMap("passwordBuckets");
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private void clearBucketMap(String fieldName) throws ReflectiveOperationException {
		var field = UserRateLimitConfig.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		((java.util.Map<?, ?>) field.get(rateLimitConfig)).clear();
	}

}
