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

package br.com.threadstech.stockfy.shared.exception;

import java.net.URI;
import java.util.List;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse.FieldError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTests {

	private static final String DETAIL = "Erro traduzido.";

	private static final URI INSTANCE = URI.create("/api/v1/users");

	@Test
	@DisplayName("Deve criar erro 400 sem erros de campo")
	void badRequest_whenFieldErrorsAreAbsent_thenCreatesResponseWithAllFields() {
		ApiErrorResponse response = ApiErrorResponse.badRequest(DETAIL, INSTANCE);

		assertResponse(response, "Validation Error", 400, DETAIL, INSTANCE, null);
	}

	@Test
	@DisplayName("Deve criar erro 400 com erro de campo explicito")
	void badRequest_whenFieldErrorIsProvided_thenCreatesResponseWithAllFields() {
		ApiErrorResponse response = ApiErrorResponse.badRequest(DETAIL, INSTANCE, "email", "E-mail invalido.");

		assertResponse(response, "Validation Error", 400, DETAIL, INSTANCE,
				List.of(new FieldError("email", "E-mail invalido.")));
	}

	@Test
	@DisplayName("Deve criar erro 409")
	void conflict_whenCalled_thenCreatesResponseWithAllFields() {
		ApiErrorResponse response = ApiErrorResponse.conflict(DETAIL, INSTANCE);

		assertResponse(response, "Conflict", 409, DETAIL, INSTANCE, null);
	}

	@Test
	@DisplayName("Deve criar erro 404")
	void notFound_whenCalled_thenCreatesResponseWithAllFields() {
		ApiErrorResponse response = ApiErrorResponse.notFound(DETAIL, INSTANCE);

		assertResponse(response, "Not Found", 404, DETAIL, INSTANCE, null);
	}

	@Test
	@DisplayName("Deve criar erro 422 sem erros de campo")
	void unprocessableEntity_whenFieldErrorsAreAbsent_thenCreatesResponseWithAllFields() {
		ApiErrorResponse response = ApiErrorResponse.unprocessableEntity(DETAIL, INSTANCE);

		assertResponse(response, "Unprocessable Entity", 422, DETAIL, INSTANCE, null);
	}

	@Test
	@DisplayName("Deve criar erro 422 com erro de campo explicito")
	void unprocessableEntity_whenFieldErrorIsProvided_thenCreatesResponseWithAllFields() {
		ApiErrorResponse response = ApiErrorResponse.unprocessableEntity(DETAIL, INSTANCE, "confirmPassword",
				"As senhas nao conferem.");

		assertResponse(response, "Unprocessable Entity", 422, DETAIL, INSTANCE,
				List.of(new FieldError("confirmPassword", "As senhas nao conferem.")));
	}

	private static void assertResponse(ApiErrorResponse response, String title, int status, String detail, URI instance,
			List<FieldError> fieldErrors) {
		assertThat(response.getType()).isEqualTo("about:blank");
		assertThat(response.getTitle()).isEqualTo(title);
		assertThat(response.getStatus()).isEqualTo(status);
		assertThat(response.getDetail()).isEqualTo(detail);
		assertThat(response.getInstance()).isEqualTo(instance);
		assertThat(response.getFieldErrors()).isEqualTo(fieldErrors);
	}

}
