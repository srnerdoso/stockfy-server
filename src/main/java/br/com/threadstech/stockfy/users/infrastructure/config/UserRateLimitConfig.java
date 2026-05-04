package br.com.threadstech.stockfy.users.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UserRateLimitConfig {

	private final TimeMeter timeMeter;

	private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

	private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

	private final Map<String, Bucket> passwordBuckets = new ConcurrentHashMap<>();

	public Bucket resolveLoginBucket(String key) {
		return loginBuckets.computeIfAbsent(key,
				k -> Bucket.builder()
					.withCustomTimePrecision(timeMeter)
					.addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
					.build());
	}

	public Bucket resolveGeneralBucket(String key) {
		return generalBuckets.computeIfAbsent(key,
				k -> Bucket.builder()
					.withCustomTimePrecision(timeMeter)
					.addLimit(Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build())
					.build());
	}

	public Bucket resolvePasswordBucket(String key) {
		return passwordBuckets.computeIfAbsent(key,
				k -> Bucket.builder()
					.withCustomTimePrecision(timeMeter)
					.addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
					.build());
	}

}
