package br.com.threadstech.stockfy.modules.users.presentation;

import br.com.threadstech.stockfy.modules.users.application.exception.EmailAlreadyExistsException;
import br.com.threadstech.stockfy.modules.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.web.application.dto.ApiErrorResponse;
import br.com.threadstech.stockfy.web.application.dto.ApiErrorResponse.FieldError;
import br.com.threadstech.stockfy.web.application.exception.InvalidPasswordException;
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
  public ResponseEntity<ApiErrorResponse> handlePasswordMismatchException(
      PasswordMismatchException ex) {
    String detail =
        messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

    ApiErrorResponse response =
        new ApiErrorResponse(
            "about:blank",
            "Unprocessable Entity",
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            detail,
            null,
            List.of(
                new FieldError(
                    "confirmPassword",
                    messageSource.getMessage(
                        "user.password.mismatch", null, LocaleContextHolder.getLocale()))));
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex) {
    String detail =
        messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

    ApiErrorResponse response =
        new ApiErrorResponse(
            "about:blank", "Conflict", HttpStatus.CONFLICT.value(), detail, null, null);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidPasswordException(
      InvalidPasswordException ex) {
    String detail =
        messageSource.getMessage(ex.getMessage(), null, LocaleContextHolder.getLocale());

    ApiErrorResponse response =
        new ApiErrorResponse(
            "about:blank",
            "Unprocessable Entity",
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            detail,
            null,
            null);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }
}
