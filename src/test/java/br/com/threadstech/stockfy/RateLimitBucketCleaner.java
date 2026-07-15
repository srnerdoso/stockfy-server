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

package br.com.threadstech.stockfy;

import java.lang.reflect.Field;
import java.util.Map;

import br.com.threadstech.stockfy.users.infrastructure.config.UserRateLimitConfig;

public final class RateLimitBucketCleaner {

	private static final String REQUEST_BUCKETS_FIELD = "requestBuckets";

	private static final String BLOCK_BUCKETS_FIELD = "blockBuckets";

	private RateLimitBucketCleaner() {
	}

	public static void clearAll(UserRateLimitConfig rateLimitConfig, MutableTimeMeter rateLimitTimeMeter) {
		clearBucketMap(rateLimitConfig, REQUEST_BUCKETS_FIELD);
		clearBucketMap(rateLimitConfig, BLOCK_BUCKETS_FIELD);
		rateLimitTimeMeter.reset();
	}

	private static void clearBucketMap(UserRateLimitConfig rateLimitConfig, String fieldName) {
		try {
			Field field = UserRateLimitConfig.class.getDeclaredField(fieldName);
			field.trySetAccessible();
			Object value = field.get(rateLimitConfig);
			if (value instanceof Map<?, ?> bucketMap) {
				bucketMap.clear();
			}
		}
		catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Failed to clear rate-limit bucket map: " + fieldName, ex);
		}
	}

}
