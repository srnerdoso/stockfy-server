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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;
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
		return this.loginBuckets.computeIfAbsent(key,
				(k) -> Bucket.builder()
					.withCustomTimePrecision(this.timeMeter)
					.addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
					.build());
	}

	public Bucket resolveGeneralBucket(String key) {
		return this.generalBuckets.computeIfAbsent(key,
				(k) -> Bucket.builder()
					.withCustomTimePrecision(this.timeMeter)
					.addLimit(Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build())
					.build());
	}

	public Bucket resolvePasswordBucket(String key) {
		return this.passwordBuckets.computeIfAbsent(key,
				(k) -> Bucket.builder()
					.withCustomTimePrecision(this.timeMeter)
					.addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
					.build());
	}

}
