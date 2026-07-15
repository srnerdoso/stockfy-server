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

package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import br.com.threadstech.stockfy.users.infrastructure.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenServiceTests {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Test
	@DisplayName("Deve salvar refresh token no Redis com usuário e expiração configurada")
	void generateRefreshToken_whenCalled_thenSavesTokenInRedisWithUserAndExpiration() {
		UUID userId = UUID.randomUUID();
		long refreshTokenExpiration = 604800000L;
		TokenService tokenService = new TokenService(this.redisTemplate);
		ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", refreshTokenExpiration);
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

		String refreshToken = tokenService.generateRefreshToken(userId);

		verify(this.valueOperations).set(keyCaptor.capture(), eq(userId.toString()), eq(refreshTokenExpiration),
				eq(TimeUnit.MILLISECONDS));
		assertThat(keyCaptor.getValue()).isEqualTo("refresh_token:" + refreshToken);
		assertThat(UUID.fromString(refreshToken).toString().equals(refreshToken)).isTrue();
	}

	@Test
	@DisplayName("Deve consumir refresh token removendo chave do Redis atomicamente")
	void consumeRefreshToken_whenTokenExists_thenReturnsUserIdAndDeletesToken() {
		UUID userId = UUID.randomUUID();
		TokenService tokenService = new TokenService(this.redisTemplate);
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		given(this.valueOperations.getAndDelete("refresh_token:refresh-token")).willReturn(userId.toString());

		var result = tokenService.consumeRefreshToken("refresh-token");

		assertThat(result.orElseThrow()).isEqualTo(userId);
	}

	@Test
	@DisplayName("Deve retornar vazio quando refresh token nao existir")
	void consumeRefreshToken_whenTokenDoesNotExist_thenReturnsEmpty() {
		TokenService tokenService = new TokenService(this.redisTemplate);
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		given(this.valueOperations.getAndDelete("refresh_token:missing-token")).willReturn(null);

		var result = tokenService.consumeRefreshToken("missing-token");

		assertThat(result.isEmpty()).isTrue();
	}

	@Test
	@DisplayName("Deve retornar vazio quando valor do refresh token estiver malformado")
	void consumeRefreshToken_whenRedisValueIsMalformed_thenReturnsEmpty() {
		TokenService tokenService = new TokenService(this.redisTemplate);
		given(this.redisTemplate.opsForValue()).willReturn(this.valueOperations);
		given(this.valueOperations.getAndDelete("refresh_token:malformed-token")).willReturn("not-a-uuid");

		var result = tokenService.consumeRefreshToken("malformed-token");

		assertThat(result.isEmpty()).isTrue();
	}

}
