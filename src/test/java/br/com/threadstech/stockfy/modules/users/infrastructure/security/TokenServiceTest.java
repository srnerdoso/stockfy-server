package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @Test
  @DisplayName("Deve salvar refresh token no Redis com usuário e expiração configurada")
  void generateRefreshToken_whenCalled_thenSavesTokenInRedisWithUserAndExpiration() {
    UUID userId = UUID.randomUUID();
    long refreshTokenExpiration = 604800000L;
    TokenService tokenService = new TokenService(redisTemplate);
    ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", refreshTokenExpiration);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

    String refreshToken = tokenService.generateRefreshToken(userId);

    verify(valueOperations)
        .set(
            keyCaptor.capture(),
            eq(userId.toString()),
            eq(refreshTokenExpiration),
            eq(TimeUnit.MILLISECONDS));
    assertEquals("refresh_token:" + refreshToken, keyCaptor.getValue());
    assertTrue(UUID.fromString(refreshToken).toString().equals(refreshToken));
  }

  @Test
  @DisplayName("Deve consumir refresh token removendo chave do Redis atomicamente")
  void consumeRefreshToken_whenTokenExists_thenReturnsUserIdAndDeletesToken() {
    UUID userId = UUID.randomUUID();
    TokenService tokenService = new TokenService(redisTemplate);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.getAndDelete("refresh_token:refresh-token")).thenReturn(userId.toString());

    var result = tokenService.consumeRefreshToken("refresh-token");

    assertEquals(userId, result.orElseThrow());
  }

  @Test
  @DisplayName("Deve retornar vazio quando refresh token nao existir")
  void consumeRefreshToken_whenTokenDoesNotExist_thenReturnsEmpty() {
    TokenService tokenService = new TokenService(redisTemplate);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.getAndDelete("refresh_token:missing-token")).thenReturn(null);

    var result = tokenService.consumeRefreshToken("missing-token");

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Deve retornar vazio quando valor do refresh token estiver malformado")
  void consumeRefreshToken_whenRedisValueIsMalformed_thenReturnsEmpty() {
    TokenService tokenService = new TokenService(redisTemplate);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.getAndDelete("refresh_token:malformed-token")).thenReturn("not-a-uuid");

    var result = tokenService.consumeRefreshToken("malformed-token");

    assertTrue(result.isEmpty());
  }
}
