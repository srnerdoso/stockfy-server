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

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage("feedback.error.validation", null,
				LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.badRequest(detail, requestUri(request),
				ex.getBindingResult().getFieldErrors(), this.messageSource, LocaleContextHolder.getLocale());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage("feedback.error.validation", null,
				LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.badRequest(detail, requestUri(request), extractFieldName(ex),
				resolveRequiredFieldMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException ex,
			HttpServletRequest request) {
		String field = ex.getParameterName();
		return validationError(field, resolveRequestParameterMessage(field, true), requestUri(request));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {
		String field = ex.getName();
		return validationError(field, resolveRequestParameterMessage(field, false), requestUri(request));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {
		ApiErrorResponse response = ApiErrorResponse.badRequest(validationDetail(), requestUri(request),
				ex.getConstraintViolations());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private ResponseEntity<ApiErrorResponse> validationError(String field, String message, URI instance) {
		ApiErrorResponse response = ApiErrorResponse.badRequest(validationDetail(), instance, field, message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private String validationDetail() {
		return this.messageSource.getMessage("feedback.error.validation", null, LocaleContextHolder.getLocale());
	}

	private String resolveRequestParameterMessage(String field, boolean required) {
		String key = required ? "validation.request-parameter.required" : "validation.request-parameter.invalid";
		return this.messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
	}

	private String resolveRequiredFieldMessage() {
		return this.messageSource.getMessage("validation.field.not-null", null, LocaleContextHolder.getLocale());
	}

	private String extractFieldName(HttpMessageNotReadableException ex) {
		if (ex.getCause() instanceof InvalidFormatException invalidFormatException
				&& !invalidFormatException.getPath().isEmpty()) {
			return invalidFormatException.getPath().getLast().getFieldName();
		}

		return "request";
	}

	private URI requestUri(HttpServletRequest request) {
		return URI.create(request.getRequestURI());
	}

}
