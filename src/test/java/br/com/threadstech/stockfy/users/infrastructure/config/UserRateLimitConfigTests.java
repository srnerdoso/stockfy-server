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

package br.com.threadstech.stockfy.users.infrastructure.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import br.com.threadstech.stockfy.MutableTimeMeter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRateLimitConfigTests {

	private static final String CLIENT_IP = "127.0.0.1";

	private static final String POST = "POST";

	private static final String REFRESH_PATH = "/api/v1/auth/sessions/refresh";

	private static final String GENERAL_POLICY = "general";

	@Test
	@DisplayName("Deve limitar refresh token a cinco requisicoes por minuto")
	void consume_whenRefreshEndpointExceedsFiveRequestsInMinute_thenReturnsRejected() {
		MutableTimeMeter timeMeter = new MutableTimeMeter();
		UserRateLimitConfig config = rateLimitConfig(timeMeter);

		for (int i = 0; i < 5; i++) {
			assertThat(config.consume(POST, REFRESH_PATH, CLIENT_IP).allowed()).isTrue();
		}

		RateLimitDecision decision = config.consume(POST, REFRESH_PATH, CLIENT_IP);

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.writeBody()).isTrue();
	}

	@Test
	@DisplayName("Deve bloquear refresh token apos quinze tentativas")
	void consume_whenRefreshEndpointExceedsFifteenAttempts_thenReturnsRejected() {
		MutableTimeMeter timeMeter = new MutableTimeMeter();
		UserRateLimitConfig config = rateLimitConfig(timeMeter);

		for (int i = 0; i < 15; i++) {
			config.consume(POST, REFRESH_PATH, CLIENT_IP);
		}

		RateLimitDecision decision = config.consume(POST, REFRESH_PATH, CLIENT_IP);

		assertThat(decision.allowed()).isFalse();
		assertThat(decision.writeBody()).isTrue();
	}

	@Test
	@DisplayName("Deve lancar excecao clara quando policy do endpoint nao existir")
	void consume_whenEndpointPolicyIsMissing_thenThrowsIllegalStateException() {
		MutableTimeMeter timeMeter = new MutableTimeMeter();
		UserRateLimitConfig config = new UserRateLimitConfig(missingPolicyProperties(), timeMeter);

		assertThatThrownBy(() -> config.consume(POST, REFRESH_PATH, CLIENT_IP))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("missing-refresh-policy");
	}

	private UserRateLimitConfig rateLimitConfig(MutableTimeMeter timeMeter) {
		return new UserRateLimitConfig(rateLimitProperties(), timeMeter);
	}

	private UserRateLimitProperties rateLimitProperties() {
		return new UserRateLimitProperties(GENERAL_POLICY,
				Map.of(GENERAL_POLICY,
						new UserRateLimitProperties.Policy(10, 10, Duration.ofMinutes(1), null, null, true), "login",
						new UserRateLimitProperties.Policy(5, 5, Duration.ofMinutes(1), null, null, true), "password",
						new UserRateLimitProperties.Policy(5, 5, Duration.ofMinutes(1), null, null, true), "refresh",
						new UserRateLimitProperties.Policy(5, 5, Duration.ofMinutes(1), 15, Duration.ofMinutes(1),
								true),
						"logout", new UserRateLimitProperties.Policy(10, 10, Duration.ofMinutes(1), null, null, false)),
				List.of(new UserRateLimitProperties.Endpoint(POST, "/api/v1/auth/sessions", "login"),
						new UserRateLimitProperties.Endpoint(POST, REFRESH_PATH, "refresh"),
						new UserRateLimitProperties.Endpoint("DELETE", "/api/v1/auth/sessions/current", "logout"),
						new UserRateLimitProperties.Endpoint("PATCH", "/api/v1/users/password", "password")));
	}

	private UserRateLimitProperties missingPolicyProperties() {
		return new UserRateLimitProperties(GENERAL_POLICY,
				Map.of(GENERAL_POLICY,
						new UserRateLimitProperties.Policy(10, 10, Duration.ofMinutes(1), null, null, true)),
				List.of(new UserRateLimitProperties.Endpoint(POST, REFRESH_PATH, "missing-refresh-policy")));
	}

}
