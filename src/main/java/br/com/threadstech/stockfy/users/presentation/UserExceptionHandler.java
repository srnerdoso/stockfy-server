package br.com.threadstech.stockfy.users.presentation;

import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse;
import br.com.threadstech.stockfy.shared.exception.ApiErrorResponse.FieldError;
import br.com.threadstech.stockfy.users.application.exception.CurrentPasswordInvalidException;
import br.com.threadstech.stockfy.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.users.application.exception.InvalidCredentialsException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.users.application.exception.InvalidPasswordResetCodeException;
import br.com.threadstech.stockfy.users.application.exception.InvalidRefreshTokenException;
import br.com.threadstech.stockfy.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.users.application.exception.UserNotFoundException;
import br.com.threadstech.stockfy.users.domain.exception.InvalidUserRolesException;
import java.util.List;
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
	public ResponseEntity<ApiErrorResponse> handlePasswordMismatchException(PasswordMismatchException ex) {
		String detail = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Unprocessable Entity",
				HttpStatus.UNPROCESSABLE_ENTITY.value(), detail, null, List.of(new FieldError("confirmPassword",
						messageSource.getMessage("user.password.mismatch", null, LocaleContextHolder.getLocale()))));
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
		String detail = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Conflict", HttpStatus.CONFLICT.value(), detail,
				null, null);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
		String detail = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Unprocessable Entity",
				HttpStatus.UNPROCESSABLE_ENTITY.value(), detail, null, null);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
	}

	@ExceptionHandler(InvalidPasswordResetCodeException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetCodeException(
			InvalidPasswordResetCodeException ex) {
		return unprocessableEntity(ex.getMessage());
	}

	@ExceptionHandler(CurrentPasswordInvalidException.class)
	public ResponseEntity<ApiErrorResponse> handleCurrentPasswordInvalidException(CurrentPasswordInvalidException ex) {
		return unprocessableEntity(ex.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
		String detail = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Not Found", HttpStatus.NOT_FOUND.value(),
				detail, null);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
		String detail = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Unauthorized", HttpStatus.UNAUTHORIZED.value(),
				detail, null);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<Void> handleInvalidRefreshTokenException() {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@ExceptionHandler(InvalidUserRolesException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidUserRolesException(InvalidUserRolesException ex) {
		String detail = messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Validation Error",
				HttpStatus.BAD_REQUEST.value(), detail, null, List.of(new FieldError("roles", detail)));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private ResponseEntity<ApiErrorResponse> unprocessableEntity(String messageKey) {
		String detail = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

		ApiErrorResponse response = new ApiErrorResponse("about:blank", "Unprocessable Entity",
				HttpStatus.UNPROCESSABLE_ENTITY.value(), detail, null, null);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
	}

}
