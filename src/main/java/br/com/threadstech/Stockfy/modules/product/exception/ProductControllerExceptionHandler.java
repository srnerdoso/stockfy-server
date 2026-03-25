package br.com.threadstech.stockfy.modules.product.exception;

import br.com.threadstech.stockfy.web.exception.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ProductControllerExceptionHandler {

  private final MessageSource messageSource;

  @ExceptionHandler(ProductUniqueViolationException.class)
  public ResponseEntity<ErrorMessage> productUniqueFieldViolationException(
      ProductUniqueViolationException ex, HttpServletRequest request) {
    Locale locale = LocaleContextHolder.getLocale();
    var params = new Object[] {ex.getFieldName()};
    String message =
        messageSource.getMessage("exception.productUniqueViolationException", params, locale);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, message));
  }
}
