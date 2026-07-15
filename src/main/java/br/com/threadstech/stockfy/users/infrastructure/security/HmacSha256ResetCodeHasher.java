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

package br.com.threadstech.stockfy.users.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import br.com.threadstech.stockfy.users.application.port.ResetCodeHasher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacSha256ResetCodeHasher implements ResetCodeHasher {

	private static final String ALGORITHM = "HmacSHA256";

	private final String secret;

	public HmacSha256ResetCodeHasher(@Value("${stockfy.users.reset-code-hash-secret}") String secret) {
		this.secret = secret;
	}

	@Override
	public String hash(String rawCode) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			SecretKeySpec keySpec = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
			mac.init(keySpec);
			return HexFormat.of().formatHex(mac.doFinal(rawCode.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			throw new IllegalStateException("Reset code hash cannot be generated", ex);
		}
	}

}
