package br.com.threadstech.stockfy.modules.users.presentation.controller;

import static br.com.threadstech.stockfy.web.presentation.ApiErrorResponseAssertions.assertBadRequestFieldValidation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.threadstech.stockfy.MutableTimeMeter;
import br.com.threadstech.stockfy.RateLimitTestConfiguration;
import br.com.threadstech.stockfy.TestcontainersConfiguration;
import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;
import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, RateLimitTestConfiguration.class })
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/base-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/users/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class GenerateResetCodeIT {

	private static final String ADMIN_ID = "00000000-0000-0000-0000-000000000001";

	private static final String OWNER_ID = "00000000-0000-0000-0000-000000000002";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ResetCodeHasher resetCodeHasher;

	@Autowired
	private UserRateLimitConfig rateLimitConfig;

	@Autowired
	private MutableTimeMeter rateLimitTimeMeter;

	@AfterEach
	void tearDownRateLimit() {
		clearRateLimitBuckets();
		rateLimitTimeMeter.reset();
	}

	@Test
	@DisplayName("Deve retornar 200, codigo e persistir hash e expiracao quando ADMIN solicitar")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenAdminRequestsExistingUser_thenReturns200AndPersistsHash() throws Exception {
		long usersBeforeRequest = countUsers();

		String response = mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isString())
			.andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("\\d{6}")))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist())
			.andExpect(jsonPath("$.resetPasswordCodeHash").doesNotExist())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String code = response.replaceAll(".*\"code\":\"(\\d{6})\".*", "$1");
		assertEquals(usersBeforeRequest, countUsers());
		assertEquals(resetCodeHasher.hash(code), resetPasswordCodeHash(OWNER_ID));
		assertFalse(code.equals(resetPasswordCodeHash(OWNER_ID)));
		assertNotNull(resetPasswordExpiresAt(OWNER_ID));
	}

	@Test
	@DisplayName("Deve sobrescrever codigo anterior quando novo codigo for gerado")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenExistingCodeExists_thenReplacesStoredHashAndExpiration() throws Exception {
		setResetPasswordData(OWNER_ID, "old-hash");

		mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isString());

		assertFalse("old-hash".equals(resetPasswordCodeHash(OWNER_ID)));
		assertNotNull(resetPasswordExpiresAt(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 401 sem corpo quando usuario nao estiver autenticado")
	void generateResetCode_whenNotAuthenticated_thenReturns401WithoutBody() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isUnauthorized())
			.andExpect(content().string(""));

		assertEquals(oldHash, resetPasswordCodeHash(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 403 sem corpo quando usuario autenticado nao for ADMIN")
	@WithMockUserId(id = OWNER_ID, roles = "USER")
	void generateResetCode_whenAuthenticatedUserIsNotAdmin_thenReturns403WithoutBody() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isForbidden())
			.andExpect(content().string(""));

		assertEquals(oldHash, resetPasswordCodeHash(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID nao for UUID valido")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenIdIsInvalidUuid_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		assertBadRequestFieldValidation(mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid")),
				"id", "O parâmetro informado é inválido.");

		assertEquals(oldHash, resetPasswordCodeHash(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 400 quando ID contiver SQL Injection")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenIdContainsSqlInjection_thenReturns400AndDoesNotAlterData() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		assertBadRequestFieldValidation(mockMvc.perform(
				post("/api/v1/users/{id}/password-reset-codes", "00000000-0000-0000-0000-000000000002' OR '1'='1")),
				"id", "O parâmetro informado é inválido.");

		assertEquals(oldHash, resetPasswordCodeHash(OWNER_ID));
	}

	@Test
	@DisplayName("Deve retornar 429 quando exceder limite geral de requisicoes")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenLimitExceeded_thenReturns429() throws Exception {
		String oldHash = resetPasswordCodeHash(OWNER_ID);

		for (int i = 0; i <= 10; i++) {
			var result = mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid"));
			if (i == 10) {
				result.andExpect(status().isTooManyRequests());
			}
		}

		assertEquals(oldHash, resetPasswordCodeHash(OWNER_ID));
	}

	@Test
	@DisplayName("Deve permitir nova geracao quando a janela de rate limit expirar")
	@WithMockUserId(id = ADMIN_ID, roles = "ADMIN")
	void generateResetCode_whenRateLimitWindowExpires_thenProcessesRequest() throws Exception {
		for (int i = 0; i < 10; i++) {
			mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid"));
		}

		mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", "not-a-uuid"))
			.andExpect(status().isTooManyRequests());

		rateLimitTimeMeter.advanceBy(Duration.ofSeconds(61));

		mockMvc.perform(post("/api/v1/users/{id}/password-reset-codes", OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isString());

		assertNotNull(resetPasswordCodeHash(OWNER_ID));
	}

	private long countUsers() {
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		return count == null ? 0 : count;
	}

	private void setResetPasswordData(String id, String hash) {
		jdbcTemplate.update("UPDATE users SET reset_password_code_hash = ?, reset_password_expires_at = now() "
				+ "WHERE id = ?::uuid", hash, id);
	}

	private String resetPasswordCodeHash(String id) {
		return jdbcTemplate.queryForObject("SELECT reset_password_code_hash FROM users WHERE id = ?::uuid",
				String.class, id);
	}

	private String resetPasswordExpiresAt(String id) {
		return jdbcTemplate.queryForObject("SELECT reset_password_expires_at::text FROM users WHERE id = ?::uuid",
				String.class, id);
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
