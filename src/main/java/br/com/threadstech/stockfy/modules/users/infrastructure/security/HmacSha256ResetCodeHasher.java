package br.com.threadstech.stockfy.modules.users.infrastructure.security;

import br.com.threadstech.stockfy.modules.users.application.port.ResetCodeHasher;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacSha256ResetCodeHasher implements ResetCodeHasher {

  private static final String ALGORITHM = "HmacSHA256";

  private final String secret;

  public HmacSha256ResetCodeHasher(
      @Value("${stockfy.users.reset-code-hash-secret:stockfy-reset-code-secret}") String secret) {
    this.secret = secret;
  }

  @Override
  public String hash(String rawCode) {
    try {
      Mac mac = Mac.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
      mac.init(keySpec);
      return HexFormat.of().formatHex(mac.doFinal(rawCode.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("Reset code hash cannot be generated", e);
    }
  }
}
