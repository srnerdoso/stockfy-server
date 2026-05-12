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

package br.com.threadstech.stockfy.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stockfy.security")
public record SecurityProperties(Jwt jwt, Cookies cookies, Hmac hmac) {

	public record Jwt(String secret, Duration accessTokenExpiration, Duration refreshTokenExpiration) {
	}

	public record Cookies(Duration accessTokenMaxAge, Duration refreshTokenMaxAge) {
	}

	public record Hmac(String secret) {
	}

}
