package br.com.threadstech.stockfy.web.dto.mapper.exception;

import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.ProductUniqueViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ApiExceptionHandler {

  private final MessageSource messageSource;
  private static final Locale locale = LocaleContextHolder.getLocale();

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorMessage> methodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request, BindingResult result) {
    return ResponseEntity.badRequest()
        .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, ex.getMessage(), result));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorMessage> entityNotFoundException(
      EntityNotFoundException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getItemNotFound()};
    String message = messageSource.getMessage("exception.entityNotFoundException", params, locale);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, message));
  }

  @ExceptionHandler(ProductUniqueViolationException.class)
  public ResponseEntity<ErrorMessage> productUniqueFieldViolationException(
      ProductUniqueViolationException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getBarCode()};
    String message =
        messageSource.getMessage("exception.productUniqueViolationException", params, locale);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, message));
  }
}
