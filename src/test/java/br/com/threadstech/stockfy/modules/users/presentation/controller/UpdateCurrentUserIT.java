package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions.assertBadRequestFieldValidation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UpdateCurrentUserIT {

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	private static final String OTHER_ID = "00000000-0000-0000-0000-000000000003";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDownRateLimit() {
		try {
			var loginBucketsField = UserRateLimitConfig.class.getDeclaredField("loginBuckets");
			loginBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) loginBucketsField.get(rateLimitConfig)).clear();

			var generalBucketsField = UserRateLimitConfig.class.getDeclaredField("generalBuckets");
			generalBucketsField.setAccessible(true);
			((java.util.Map<?, ?>) generalBucketsField.get(rateLimitConfig)).clear();
			rateLimitTimeMeter.reset();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@DisplayName("Deve retornar 204 e persistir nome e email quando usuario autenticado atualizar perfil")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenRequestIsValid_thenReturns204AndPersistsData() throws Exception {
		long usersBeforeRequest = countUsers();
		String json = """
				{
				  "name": "Bruno Updated",
				  "email": "bruno.updated@example.com"
				}
				""";

		mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("Bruno Updated", userName(OWNER_ID));
		assertEquals("bruno.updated@example.com", userEmail(OWNER_ID));
		assertUserHasRoles(OWNER_ID, "USER");
		assertEquals("ACTIVE", userStatus(OWNER_ID));
		assertTrue(userIsActive(OWNER_ID));
	}

	@Test
	@DisplayName("Deve atualizar somente nome quando email for omitido")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenEmailIsOmitted_thenKeepsCurrentEmail() throws Exception {
		long usersBeforeRequest = countUsers();

		mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Only Name\"}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("Only Name", userName(OWNER_ID));
		assertEquals("bruno.user@example.com", userEmail(OWNER_ID));
	}

	@Test
	@DisplayName("Deve atualizar somente email quando nome for omitido")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenNameIsOmitted_thenKeepsCurrentName() throws Exception {
		long usersBeforeRequest = countUsers();

		mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"only.email@example.com\"}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("Bruno User", userName(OWNER_ID));
		assertEquals("only.email@example.com", userEmail(OWNER_ID));
	}

	@Test
	@DisplayName("Nao deve aplicar campos protegidos da entidade enviados no corpo")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenRoleAndStatusAreSent_thenDoesNotAlterRoleOrStatus() throws Exception {
		long usersBeforeRequest = countUsers();
		String originalCreatedAt = userCreatedAt(OWNER_ID);
		String originalCreatedBy = userCreatedBy(OWNER_ID);
		setResetPasswordData(OWNER_ID);
		String json = """
				{
				  "id": "99999999-9999-9999-9999-999999999999",
				  "name": "Bruno Safe",
				  "email": "bruno.safe@example.com",
				  "password": "new-password",
				  "passwordHash": "leaked-password-hash",
				  "roles": ["ADMIN"],
				  "status": "LOCKED",
				  "active": false,
				  "resetCode": "123456",
				  "resetPasswordCodeHash": "leaked-reset-hash",
				  "resetPasswordExpiresAt": "2099-12-31T23:59:59",
				  "createdAt": "2099-01-01T00:00:00",
				  "createdBy": "99999999-9999-9999-9999-999999999999",
				  "updatedAt": "1999-01-01T00:00:00",
				  "updatedBy": "99999999-9999-9999-9999-999999999999"
				}
				""";

		mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userExists(OWNER_ID));
		assertFalse(userExists("99999999-9999-9999-9999-999999999999"));
		assertEquals("Bruno Safe", userName(OWNER_ID));
		assertEquals("bruno.safe@example.com", userEmail(OWNER_ID));
		assertUserHasRoles(OWNER_ID, "USER");
		assertEquals("ACTIVE", userStatus(OWNER_ID));
		assertTrue(userIsActive(OWNER_ID));
		assertEquals("password-hash-2", passwordHash(OWNER_ID));
		assertEquals("existing-reset-hash", resetPasswordCodeHash(OWNER_ID));
		assertTrue(resetPasswordExpiresAt(OWNER_ID).startsWith("2026-05-01"));
		assertEquals(originalCreatedAt, userCreatedAt(OWNER_ID));
		assertEquals(originalCreatedBy, userCreatedBy(OWNER_ID));
		assertFalse(userUpdatedAt(OWNER_ID).startsWith("1999-01-01"));
		assertFalse("99999999-9999-9999-9999-999999999999".equals(userUpdatedBy(OWNER_ID)));
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void updateCurrentUser_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		long usersBeforeRequest = countUsers();

		mockMvc
			.perform(
					patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"No Auth\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("Bruno User", userName(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando email for invalido")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenEmailIsInvalid_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
					.content("{\"email\":\"invalid-email\"}")),
				"email", "O e-mail informado é inválido.");

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("bruno.user@example.com", userEmail(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando nome contiver SQL Injection")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenNameContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		assertBadRequestFieldValidation(
				mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"User'); DROP TABLE users; --\"}")),
				"name", "O nome contém caracteres inválidos.");

		assertEquals(usersBeforeRequest, countUsers());
		assertTrue(userExists(OWNER_ID));
		assertTrue(userExists(OTHER_ID));
	}

	@Test
	@DisplayName("Deve retornar 409 quando email pertencer a outro usuario")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenEmailBelongsToAnotherUser_thenReturns409AndDoesNotAlterData() throws Exception {
		long usersBeforeRequest = countUsers();

		mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"alice.filter@example.com\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.type").value("about:blank"))
			.andExpect(jsonPath("$.title").value("Conflict"))
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.detail").value("O e-mail informado já está em uso."))
			.andExpect(jsonPath("$.instance").doesNotExist())
			.andExpect(jsonPath("$.fieldErrors").doesNotExist());

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("bruno.user@example.com", userEmail(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenLimitExceeded_thenReturns429() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i <= 10; i++) {
			var result = mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"invalid-email\"}"));
			if (i == 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("bruno.user@example.com", userEmail(OWNER_ID));
	}

	@Test
	@DisplayName("Deve permitir nova atualizacao quando a janela de rate limit expirar")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void updateCurrentUser_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		long usersBeforeRequest = countUsers();

		for (int i = 0; i < 10; i++) {
			mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"invalid-email\"}"));
		}

		mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"invalid-email\"}"))
			.andExpect(status().isTooManyRequests());

		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		mockMvc
			.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"After Window\"}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		assertEquals(usersBeforeRequest, countUsers());
		assertEquals("After Window", userName(OWNER_ID));
	}

	private long countUsers() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return count == null ? 0 : count;
	}

	private boolean userExists(String id) {
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?::uuid", Integer.class, id);
		return count != null && count == 1;
	}

	private String userName(String id) {
		return jdbcTemplate.queryForObject("SELECT name FROM users WHERE id = ?::uuid", String.class, id);
	}

	private String userEmail(String id) {
		return jdbcTemplate.queryForObject("SELECT email FROM users WHERE id = ?::uuid", String.class, id);
	}

	private void assertUserHasRoles(String id, String... expectedRoles) {
		var roles = jdbcTemplate.queryForList("SELECT role FROM users_roles WHERE user_id = ?::uuid ORDER BY role",
				String.class, id);
		assertEquals(expectedRoles.length, roles.size());
		for (String expectedRole : expectedRoles) {
			assertTrue(roles.contains(expectedRole));
		}
	}

	private String userStatus(String id) {
		return jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?::uuid", String.class, id);
	}

	private boolean userIsActive(String id) {
		Boolean active = jdbcTemplate.queryForObject("SELECT active FROM users WHERE id = ?::uuid", Boolean.class, id);
		return Boolean.TRUE.equals(active);
	}

	private String passwordHash(String id) {
		return jdbcTemplate.queryForObject("SELECT password_hash FROM users WHERE id = ?::uuid", String.class, id);
	}

	private void setResetPasswordData(String id) {
		jdbcTemplate.update(
				"UPDATE users SET reset_password_code_hash = ?, reset_password_expires_at = ? " + "WHERE id = ?::uuid",
				"existing-reset-hash", java.sql.Timestamp.valueOf("2026-05-01 10:00:00"), id);
	}

	private String resetPasswordCodeHash(String id) {
		return jdbcTemplate.queryForObject("SELECT reset_password_code_hash FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private String resetPasswordExpiresAt(String id) {
		return jdbcTemplate.queryForObject("SELECT reset_password_expires_at::text FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private String userCreatedAt(String id) {
		return jdbcTemplate.queryForObject("SELECT created_at::text FROM users WHERE id = ?::uuid", String.class, id);
	}

	private String userCreatedBy(String id) {
		return jdbcTemplate.queryForObject("SELECT created_by::text FROM users WHERE id = ?::uuid", String.class, id);
	}

	private String userUpdatedAt(String id) {
		return jdbcTemplate.queryForObject("SELECT updated_at::text FROM users WHERE id = ?::uuid", String.class, id);
	}

	private String userUpdatedBy(String id) {
		return jdbcTemplate.queryForObject("SELECT updated_by::text FROM users WHERE id = ?::uuid", String.class, id);
	}

}
