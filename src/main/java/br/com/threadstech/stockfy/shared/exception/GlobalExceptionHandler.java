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

import java.util.List;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse.FieldError;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
	public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String detail = messageSource.getMessage("feedback.error.validation", null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error",
				HttpStatus.BAD_REQUEST.value(), detail, null,
				ex.getBindingResult()
					.getFieldErrors()
					.stream()
					.map(fieldError -> new FieldError(fieldError.getField(),
							messageSource.getMessage(fieldError, LocaleContextHolder.getLocale())))
					.toList());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
		String detail = messageSource.getMessage("feedback.error.validation", null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error",
				HttpStatus.BAD_REQUEST.value(), detail, null,
				List.of(new FieldError(extractFieldName(ex), resolveRequiredFieldMessage())));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException ex) {
		String field = ex.getParameterName();
		return validationError(field, resolveRequestParameterMessage(field, true));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String field = ex.getName();
		return validationError(field, resolveRequestParameterMessage(field, false));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
		List<FieldError> fieldErrors = ex.getConstraintViolations()
			.stream()
			.map(violation -> new FieldError(extractLeafProperty(violation.getPropertyPath().toString()),
					violation.getMessage()))
			.toList();

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error",
				HttpStatus.BAD_REQUEST.value(), validationDetail(), null, fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private ResponseEntity<ApiErrorResponse> validationError(String field, String message) {
		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error",
				HttpStatus.BAD_REQUEST.value(), validationDetail(), null, List.of(new FieldError(field, message)));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private String validationDetail() {
		return messageSource.getMessage("feedback.error.validation", null, LocaleContextHolder.getLocale());
	}

	private String resolveRequestParameterMessage(String field, boolean required) {
		String key = required ? "validation.request-parameter.required" : "validation.request-parameter.invalid";
		return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
	}

	private String extractLeafProperty(String propertyPath) {
		int separator = propertyPath.lastIndexOf('.');
		if (separator < 0) {
			return propertyPath;
		}
		return propertyPath.substring(separator + 1);
	}

	private String resolveRequiredFieldMessage() {
		return messageSource.getMessage("validation.field.not-null", null, LocaleContextHolder.getLocale());
	}

	private String extractFieldName(HttpMessageNotReadableException ex) {
		if (ex.getCause() instanceof InvalidFormatException invalidFormatException
				&& !invalidFormatException.getPath().isEmpty()) {
			return invalidFormatException.getPath().getLast().getFieldName();
		}

		return "request";
	}

}
