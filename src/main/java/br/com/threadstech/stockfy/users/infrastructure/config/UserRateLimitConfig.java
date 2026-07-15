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

	private final UserRateLimitProperties properties;

	private final TimeMeter timeMeter;

	private final Map<String, Bucket> requestBuckets = new ConcurrentHashMap<>();

	private final Map<String, Bucket> blockBuckets = new ConcurrentHashMap<>();

	public RateLimitDecision consume(String method, String path, String clientIp) {
		String policyName = resolvePolicyName(method, path);
		UserRateLimitProperties.Policy policy = this.properties.policies().get(policyName);
		if (policy == null) {
			throw new IllegalStateException("Rate-limit policy not found: " + policyName);
		}
		if (policy.blockCapacity() != null && !resolveBlockBucket(policyName, clientIp, policy).tryConsume(1)) {
			return RateLimitDecision.reject(policy.writeBody());
		}
		if (resolveRequestBucket(policyName, clientIp, policy).tryConsume(1)) {
			return RateLimitDecision.allow();
		}
		return RateLimitDecision.reject(policy.writeBody());
	}

	private String resolvePolicyName(String method, String path) {
		return this.properties.endpoints()
			.stream()
			.filter((endpoint) -> endpoint.method().equals(method) && endpoint.path().equals(path))
			.map(UserRateLimitProperties.Endpoint::policy)
			.findFirst()
			.orElse(this.properties.defaultPolicy());
	}

	private Bucket resolveRequestBucket(String policyName, String clientIp, UserRateLimitProperties.Policy policy) {
		String key = policyName + ":" + clientIp;
		return this.requestBuckets.computeIfAbsent(key,
				(ignored) -> Bucket.builder()
					.withCustomTimePrecision(this.timeMeter)
					.addLimit(Bandwidth.builder()
						.capacity(policy.capacity())
						.refillGreedy(policy.refillTokens(), policy.refillPeriod())
						.build())
					.build());
	}

	private Bucket resolveBlockBucket(String policyName, String clientIp, UserRateLimitProperties.Policy policy) {
		String key = policyName + ":" + clientIp;
		Duration blockRefillPeriod = (policy.blockRefillPeriod() != null) ? policy.blockRefillPeriod()
				: policy.refillPeriod();
		return this.blockBuckets.computeIfAbsent(key,
				(ignored) -> Bucket.builder()
					.withCustomTimePrecision(this.timeMeter)
					.addLimit(Bandwidth.builder()
						.capacity(policy.blockCapacity())
						.refillGreedy(policy.blockCapacity(), blockRefillPeriod)
						.build())
					.build());
	}

}
