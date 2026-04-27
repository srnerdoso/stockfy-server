package br.com.threadstech.stockfy.web.presentation;

import br.com.threadstech.stockfy.web.application.dto.ApiErrorResponse;
import br.com.threadstech.stockfy.web.application.dto.ApiErrorResponse.FieldError;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    String detail =
        messageSource.getMessage(
            "feedback.error.validation", null, LocaleContextHolder.getLocale());

    ApiErrorResponse response =
        new ApiErrorResponse(
            "about:blank",
            "Validation Error",
            HttpStatus.BAD_REQUEST.value(),
            detail,
            null,
            ex.getBindingResult().getFieldErrors().stream()
                .map(
                    fieldError ->
                        new FieldError(
                            fieldError.getField(),
                            messageSource.getMessage(
                                fieldError, LocaleContextHolder.getLocale())))
                .toList());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
      HttpMessageNotReadableException ex) {
    String detail =
        messageSource.getMessage(
            "feedback.error.validation", null, LocaleContextHolder.getLocale());

    ApiErrorResponse response =
        new ApiErrorResponse(
            "about:blank",
            "Validation Error",
            HttpStatus.BAD_REQUEST.value(),
            detail,
            null,
            List.of(new FieldError(extractFieldName(ex), resolveRequiredFieldMessage())));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  private String resolveRequiredFieldMessage() {
    return messageSource.getMessage(
        "validation.field.not-null", null, LocaleContextHolder.getLocale());
  }

  private String extractFieldName(HttpMessageNotReadableException ex) {
    if (ex.getCause() instanceof InvalidFormatException invalidFormatException
        && !invalidFormatException.getPath().isEmpty()) {
      return invalidFormatException.getPath().getLast().getFieldName();
    }

    return "request";
  }
}
