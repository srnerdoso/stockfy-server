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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stockfy.users.rate-limit")
public record UserRateLimitProperties(String defaultPolicy, Map<String, Policy> policies, List<Endpoint> endpoints) {

	public UserRateLimitProperties {
		policies = Map.copyOf(policies);
		endpoints = List.copyOf(endpoints);
	}

	public record Policy(int capacity, int refillTokens, Duration refillPeriod, Integer blockCapacity,
			Duration blockRefillPeriod, boolean writeBody) {
	}

	public record Endpoint(String method, String path, String policy) {
	}

}
