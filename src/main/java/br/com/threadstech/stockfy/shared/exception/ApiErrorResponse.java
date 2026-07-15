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
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

@Getter
public final class ApiErrorResponse {

	private final String type;

	private final String title;

	private final int status;

	private final String detail;

	private final URI instance;

	private final List<FieldError> fieldErrors;

	private ApiErrorResponse(String type, String title, int status, String detail, URI instance,
			List<FieldError> fieldErrors) {
		this.type = type;
		this.title = title;
		this.status = status;
		this.detail = detail;
		this.instance = instance;
		this.fieldErrors = (fieldErrors != null) ? List.copyOf(fieldErrors) : null;
	}

	public static ApiErrorResponse badRequest(String detail, URI instance) {
		return of(HttpStatus.BAD_REQUEST, validationErrorTitle(), detail, instance, null);
	}

	public static ApiErrorResponse badRequest(String detail, URI instance, String field, String message) {
		return of(HttpStatus.BAD_REQUEST, validationErrorTitle(), detail, instance, apiFieldErrors(field, message));
	}

	public static ApiErrorResponse badRequest(String detail, URI instance,
			List<org.springframework.validation.FieldError> fieldErrors, MessageSource messageSource, Locale locale) {
		return of(HttpStatus.BAD_REQUEST, validationErrorTitle(), detail, instance,
				apiFieldErrors(fieldErrors, messageSource, locale));
	}

	public static ApiErrorResponse badRequest(String detail, URI instance, Set<ConstraintViolation<?>> violations) {
		return of(HttpStatus.BAD_REQUEST, validationErrorTitle(), detail, instance, apiFieldErrors(violations));
	}

	public static ApiErrorResponse conflict(String detail, URI instance) {
		return of(HttpStatus.CONFLICT, detail, instance);
	}

	public static ApiErrorResponse notFound(String detail, URI instance) {
		return of(HttpStatus.NOT_FOUND, detail, instance);
	}

	public static ApiErrorResponse unprocessableEntity(String detail, URI instance) {
		return of(HttpStatus.UNPROCESSABLE_ENTITY, detail, instance);
	}

	public static ApiErrorResponse unprocessableEntity(String detail, URI instance, String field, String message) {
		return of(HttpStatus.UNPROCESSABLE_ENTITY, detail, instance, apiFieldErrors(field, message));
	}

	private static ApiErrorResponse of(HttpStatus status, String detail, URI instance) {
		return of(status, status.getReasonPhrase(), detail, instance, null);
	}

	private static ApiErrorResponse of(HttpStatus status, String detail, URI instance, List<FieldError> fieldErrors) {
		return of(status, status.getReasonPhrase(), detail, instance, fieldErrors);
	}

	private static ApiErrorResponse of(HttpStatus status, String title, String detail, URI instance,
			List<FieldError> fieldErrors) {
		return new ApiErrorResponse("about:blank", title, status.value(), detail, instance, fieldErrors);
	}

	private static String validationErrorTitle() {
		return "Validation Error";
	}

	private static List<FieldError> apiFieldErrors(String field, String message) {
		return List.of(new FieldError(field, message));
	}

	private static List<FieldError> apiFieldErrors(List<org.springframework.validation.FieldError> fieldErrors,
			MessageSource messageSource, Locale locale) {
		return fieldErrors.stream()
			.map((fieldError) -> new FieldError(fieldError.getField(), messageSource.getMessage(fieldError, locale)))
			.toList();
	}

	private static List<FieldError> apiFieldErrors(Set<ConstraintViolation<?>> violations) {
		return violations.stream()
			.map((violation) -> new FieldError(extractLeafProperty(violation.getPropertyPath().toString()),
					violation.getMessage()))
			.toList();
	}

	private static String extractLeafProperty(String propertyPath) {
		int separator = propertyPath.lastIndexOf('.');
		if (separator < 0) {
			return propertyPath;
		}
		return propertyPath.substring(separator + 1);
	}

	public record FieldError(String field, String message) {
	}

}
