package br.com.threadstech.stockfy.modules.users.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

  private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
  private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

  public Bucket resolveLoginBucket(String key) {
    return loginBuckets.computeIfAbsent(
        key,
        k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(15, Refill.greedy(15, Duration.ofMinutes(1))))
                .build());
  }

  public Bucket resolveGeneralBucket(String key) {
    return generalBuckets.computeIfAbsent(
        key,
        k ->
            Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build());
  }
}
