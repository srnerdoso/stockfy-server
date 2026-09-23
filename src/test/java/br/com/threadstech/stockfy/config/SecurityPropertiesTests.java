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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTests {

	@Test
	@DisplayName("Deve atribuir SameSite=Strict por padrao quando o valor for nulo ou vazio")
	void cookies_whenSameSiteIsNullOrBlank_thenDefaultsToStrict() {
		var cookiesWithNull = new SecurityProperties.Cookies(Duration.ofMinutes(15), Duration.ofDays(7), null, true);
		assertThat(cookiesWithNull.sameSite()).isEqualTo("Strict");

		var cookiesWithBlank = new SecurityProperties.Cookies(Duration.ofMinutes(15), Duration.ofDays(7), "   ", true);
		assertThat(cookiesWithBlank.sameSite()).isEqualTo("Strict");
	}

	@Test
	@DisplayName("Deve respeitar atributos explicitamente informados")
	void cookies_whenValuesAreExplicitlyProvided_thenPreservesValues() {
		var cookies = new SecurityProperties.Cookies(Duration.ofMinutes(15), Duration.ofDays(7), "Lax", false);
		assertThat(cookies.sameSite()).isEqualTo("Lax");
		assertThat(cookies.secure()).isFalse();
	}

}
