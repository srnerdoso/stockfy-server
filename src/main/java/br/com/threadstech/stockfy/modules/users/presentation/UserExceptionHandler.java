package br.com.threadstech.stockfy.modules.users.presentation;

import br.com.threadstech.stockfy.modules.users.application.exception.PasswordMismatchException;
import br.com.threadstech.stockfy.web.application.dto.ApiErrorResponse;
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
            null);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }
}
