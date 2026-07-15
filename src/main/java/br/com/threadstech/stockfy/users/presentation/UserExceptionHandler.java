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

package br.com.threadstech.stockfy.users.presentation;

import java.net.URI;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse;
import br.com.threadstech.stockfy.users.application.exception.CurrentPasswordInvalidException;
import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordResetCodeException;
import br.com.threadstech.stockfy.users.application.exception.InvalidRefreshTokenException;
import br.com.threadstech.stockfy.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.exception.InvalidUserRolesException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class UserExceptionHandler {

	private final MessageSource messageSource;

	@ExceptionHandler(PasswordMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handlePasswordMismatchException(PasswordMismatchException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.unprocessableEntity(detail, requestUri(request), "confirmPassword",
				this.messageSource.getMessage("user.password.mismatch", null, LocaleContextHolder.getLocale()));
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.conflict(detail, requestUri(request));
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.unprocessableEntity(detail, requestUri(request));
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
	}

	@ExceptionHandler(InvalidPasswordResetCodeException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetCodeException(
			InvalidPasswordResetCodeException ex, HttpServletRequest request) {
		return unprocessableEntity(ex.getMessage(), requestUri(request));
	}

	@ExceptionHandler(CurrentPasswordInvalidException.class)
	public ResponseEntity<ApiErrorResponse> handleCurrentPasswordInvalidException(CurrentPasswordInvalidException ex,
			HttpServletRequest request) {
		return unprocessableEntity(ex.getMessage(), requestUri(request));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.notFound(detail, requestUri(request));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<Void> handleInvalidCredentialsException() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<Void> handleInvalidRefreshTokenException() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(InvalidUserRolesException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidUserRolesException(InvalidUserRolesException ex,
			HttpServletRequest request) {
		String detail = this.messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.badRequest(detail, requestUri(request), "roles", detail);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private ResponseEntity<ApiErrorResponse> unprocessableEntity(String messageKey, URI instance) {
		String detail = this.messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = ApiErrorResponse.unprocessableEntity(detail, instance);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
	}

	private URI requestUri(HttpServletRequest request) {
		return URI.create(request.getRequestURI());
	}

}
