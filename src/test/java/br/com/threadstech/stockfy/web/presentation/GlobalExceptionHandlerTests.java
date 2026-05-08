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

package br.com.threadstech.stockfy.web.presentation;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse;
import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse.FieldError;
import br.com.threadstech.stockfy.shared.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTests {

	private static final String VALIDATION_FEEDBACK_KEY = "feedback.error.validation";

	private static final String TRANSLATED_ERROR = "Erro traduzido.";

	private static final String TRANSLATED_FIELD_MESSAGE = "Campo traduzido.";

	private static final String TYPE_FIELD = "type";

	private static final String ROLE_FIELD = "role";

	private static final URI REQUEST_URI = URI.create("/api/v1/users");

	@Test
	@DisplayName("Deve traduzir mensagem de campo inválido usando MessageSource")
	void handleMessageNotReadable_whenFieldIsInvalid_thenUsesMessageSource() {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(VALIDATION_FEEDBACK_KEY, Locale.getDefault(), TRANSLATED_ERROR);
		messageSource.addMessage("validation.field.not-null", Locale.getDefault(), TRANSLATED_FIELD_MESSAGE);
		GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
		InvalidFormatException cause = InvalidFormatException.from(null, "Invalid value", "INVALID", String.class);
		cause.prependPath(new Object(), ROLE_FIELD);
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid JSON", cause,
				new MockHttpInputMessage(new byte[0]));
		MockHttpServletRequest request = request();

		ApiErrorResponse body = handler.handleMessageNotReadable(exception, request).getBody();

		assertResponse(body, ROLE_FIELD, TRANSLATED_FIELD_MESSAGE);
	}

	@Test
	@DisplayName("Deve usar mensagem generica quando parametro obrigatorio estiver ausente")
	void handleMissingRequestParameter_whenTypeIsMissing_thenUsesGenericMessage() {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(VALIDATION_FEEDBACK_KEY, Locale.getDefault(), TRANSLATED_ERROR);
		messageSource.addMessage("validation.request-parameter.required", Locale.getDefault(),
				"Parametro obrigatorio generico.");
		GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
		MissingServletRequestParameterException exception = new MissingServletRequestParameterException(TYPE_FIELD,
				"UserListType");
		MockHttpServletRequest request = request();

		ApiErrorResponse body = handler.handleMissingRequestParameter(exception, request).getBody();

		assertResponse(body, TYPE_FIELD, "Parametro obrigatorio generico.");
	}

	@Test
	@DisplayName("Deve usar mensagem generica quando parametro possuir tipo invalido")
	void handleArgumentTypeMismatch_whenTypeIsInvalid_thenUsesGenericMessage() throws NoSuchMethodException {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(VALIDATION_FEEDBACK_KEY, Locale.getDefault(), TRANSLATED_ERROR);
		messageSource.addMessage("validation.request-parameter.invalid", Locale.getDefault(),
				"Parametro invalido generico.");
		GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
		Method method = GlobalExceptionHandlerTests.class.getDeclaredMethod("methodWithTypeParameter", String.class);
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("FULL", String.class,
				TYPE_FIELD, new MethodParameter(method, 0), null);
		MockHttpServletRequest request = request();

		ApiErrorResponse body = handler.handleArgumentTypeMismatch(exception, request).getBody();

		assertResponse(body, TYPE_FIELD, "Parametro invalido generico.");
	}

	private void assertResponse(ApiErrorResponse body, String field, String message) {
		assertThat(body.getType()).isEqualTo("about:blank");
		assertThat(body.getTitle()).isEqualTo("Validation Error");
		assertThat(body.getStatus()).isEqualTo(400);
		assertThat(body.getDetail()).isEqualTo(TRANSLATED_ERROR);
		assertThat(body.getInstance()).isEqualTo(REQUEST_URI);
		assertThat(body.getFieldErrors()).containsExactly(new FieldError(field, message));
	}

	private MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(REQUEST_URI.toString());
		return request;
	}

	private void methodWithTypeParameter(String type) {
	}

}
