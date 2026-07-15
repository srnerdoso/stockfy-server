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

import br.com.threadstech.stockfy.config.SecurityProperties;
import br.com.threadstech.stockfy.users.infrastructure.security.HmacSha256Hasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HmacSha256HasherTests {

	@Test
	@DisplayName("Deve gerar hash HMAC SHA-256 deterministico")
	void hash_whenCalledWithSameRawValue_thenReturnsSameHash() {
		SecurityProperties properties = new SecurityProperties(null, null, new SecurityProperties.Hmac("secret-key"));
		HmacSha256Hasher hasher = new HmacSha256Hasher(properties);

		String first = hasher.hash("raw-value");
		String second = hasher.hash("raw-value");

		assertThat(first).hasSize(64);
		assertThat(second).isEqualTo(first);
	}

	@Test
	@DisplayName("Deve gerar hashes diferentes para entradas diferentes")
	void hash_whenCalledWithDifferentRawValues_thenReturnsDifferentHashes() {
		SecurityProperties properties = new SecurityProperties(null, null, new SecurityProperties.Hmac("secret-key"));
		HmacSha256Hasher hasher = new HmacSha256Hasher(properties);

		String first = hasher.hash("value-1");
		String second = hasher.hash("value-2");

		assertThat(first).isNotEqualTo(second);
	}

}
