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

import java.util.List;
import java.util.Locale;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse;
import br.com.threadstech.stockfy.shared.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

	@Test
	@DisplayName("Deve traduzir mensagem de campo inválido usando MessageSource")
	void handleMessageNotReadable_whenFieldIsInvalid_thenUsesMessageSource() {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage("feedback.error.validation", Locale.getDefault(), "Erro traduzido.");
		messageSource.addMessage("validation.field.not-null", Locale.getDefault(), "Campo traduzido.");
		GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
		InvalidFormatException cause = InvalidFormatException.from(null, "Invalid value", "INVALID", String.class);
		cause.prependPath(new Object(), "role");
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid JSON", cause,
				new MockHttpInputMessage(new byte[0]));

		ApiErrorResponse body = handler.handleMessageNotReadable(exception).getBody();

		assertEquals("Erro traduzido.", body.getDetail());
		assertEquals("role", body.getFieldErrors().getFirst().field());
		assertEquals("Campo traduzido.", body.getFieldErrors().getFirst().message());
	}

	@Test
	@DisplayName("Deve usar mensagem generica quando parametro obrigatorio estiver ausente")
	void handleMissingRequestParameter_whenTypeIsMissing_thenUsesGenericMessage() {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage("feedback.error.validation", Locale.getDefault(), "Erro traduzido.");
		messageSource.addMessage("validation.request-parameter.required", Locale.getDefault(),
				"Parametro obrigatorio generico.");
		GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
		MissingServletRequestParameterException exception = new MissingServletRequestParameterException("type",
				"UserListType");

		ApiErrorResponse body = handler.handleMissingRequestParameter(exception).getBody();

		assertEquals("Erro traduzido.", body.getDetail());
		assertEquals("type", body.getFieldErrors().getFirst().field());
		assertEquals("Parametro obrigatorio generico.", body.getFieldErrors().getFirst().message());
	}

	@Test
	@DisplayName("Deve usar mensagem generica quando parametro possuir tipo invalido")
	void handleArgumentTypeMismatch_whenTypeIsInvalid_thenUsesGenericMessage() {
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage("feedback.error.validation", Locale.getDefault(), "Erro traduzido.");
		messageSource.addMessage("validation.request-parameter.invalid", Locale.getDefault(),
				"Parametro invalido generico.");
		GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("FULL", String.class,
				"type", null, null);

		ApiErrorResponse body = handler.handleArgumentTypeMismatch(exception).getBody();

		assertEquals("Erro traduzido.", body.getDetail());
		assertEquals("type", body.getFieldErrors().getFirst().field());
		assertEquals("Parametro invalido generico.", body.getFieldErrors().getFirst().message());
	}

	@Test
	@DisplayName("Deve expor erros de campo sem depender de FieldError do Spring")
	void constructor_whenFieldErrorsAreProvided_thenUsesApiFieldError() {
		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error", 400, "Erro traduzido.",
				null, List.of(new ApiErrorResponse.FieldError("role", "Campo traduzido.")));

		assertEquals("role", response.getFieldErrors().getFirst().field());
		assertEquals("Campo traduzido.", response.getFieldErrors().getFirst().message());
	}

}
