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
import java.util.List;
import java.util.Locale;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse;
import br.com.threadstech.stockfy.shared.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTests {

	private static final String VALIDATION_FEEDBACK_KEY = "feedback.error.validation";

	private static final String TRANSLATED_ERROR = "Erro traduzido.";

	private static final String TRANSLATED_FIELD_MESSAGE = "Campo traduzido.";

	private static final String TYPE_FIELD = "type";

	private static final String ROLE_FIELD = "role";

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

		ApiErrorResponse body = handler.handleMessageNotReadable(exception).getBody();

		assertThat(body.getDetail()).isEqualTo(TRANSLATED_ERROR);
		assertThat(body.getFieldErrors().getFirst().field()).isEqualTo(ROLE_FIELD);
		assertThat(body.getFieldErrors().getFirst().message()).isEqualTo(TRANSLATED_FIELD_MESSAGE);
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

		ApiErrorResponse body = handler.handleMissingRequestParameter(exception).getBody();

		assertThat(body.getDetail()).isEqualTo(TRANSLATED_ERROR);
		assertThat(body.getFieldErrors().getFirst().field()).isEqualTo(TYPE_FIELD);
		assertThat(body.getFieldErrors().getFirst().message()).isEqualTo("Parametro obrigatorio generico.");
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

		ApiErrorResponse body = handler.handleArgumentTypeMismatch(exception).getBody();

		assertThat(body.getDetail()).isEqualTo(TRANSLATED_ERROR);
		assertThat(body.getFieldErrors().getFirst().field()).isEqualTo(TYPE_FIELD);
		assertThat(body.getFieldErrors().getFirst().message()).isEqualTo("Parametro invalido generico.");
	}

	@Test
	@DisplayName("Deve expor erros de campo sem depender de FieldError do Spring")
	void constructor_whenFieldErrorsAreProvided_thenUsesApiFieldError() {
		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error", 400, TRANSLATED_ERROR, null,
				List.of(new ApiErrorResponse.FieldError(ROLE_FIELD, TRANSLATED_FIELD_MESSAGE)));

		assertThat(response.getFieldErrors().getFirst().field()).isEqualTo(ROLE_FIELD);
		assertThat(response.getFieldErrors().getFirst().message()).isEqualTo(TRANSLATED_FIELD_MESSAGE);
	}

	private void methodWithTypeParameter(String type) {
	}

}
