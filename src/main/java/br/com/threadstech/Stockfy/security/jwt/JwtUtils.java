package br.com.threadstech.stockfy.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtUtils {

  private String secret;
  private int expireDays;
  private int expireHours;
  private int expireMinutes;

  private SecretKey generateKey() {
    if (secret == null) {
      throw new IllegalArgumentException("Secret key must not be null");
    }
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  private Date toExpireDate(Date start) {
    LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime end =
        dateTime.plusDays(expireDays).plusHours(expireHours).plusMinutes(expireMinutes);
    return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
  }

  public JwtToken createToken(Long userId, String username, String role) {
    Date issuedAt = new Date();
    Date expiresAt = toExpireDate(issuedAt);
    String token =
        Jwts.builder()
            .header()
            .add("typ", "JWT")
            .and()
            .subject(userId.toString())
            .issuedAt(issuedAt)
            .expiration(expiresAt)
            .signWith(generateKey())
            .claim("username", username)
            .claim("role", role)
            .compact();
    return new JwtToken(token);
  }

  public Long getIdFromToken(String token) {
    Claims claims = getClaims(token);
    assert claims != null;
    return Long.parseLong(claims.getSubject());
  }

  public String getUsernameFromToken(String token) {
    Claims claims = getClaims(token);
    assert claims != null;
    return claims.get("username", String.class);
  }

  private Claims getClaims(String token) {
    try {
      return Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(token).getPayload();
    } catch (JwtException ex) {
      log.error("Failed to parse JWT token: {}", ex.getMessage());
      return null;
    }
  }

  public boolean isTokenValid(String token) {
    try {
      Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(token);
      return true;
    } catch (JwtException ex) {
      log.error("Invalid JWT token: {}", ex.getMessage());
      return false;
    }
  }
}
