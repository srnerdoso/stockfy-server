package br.com.threadstech.stockfy.web.exception;

import br.com.threadstech.stockfy.exception.CustomerUniqueViolationException;
import br.com.threadstech.stockfy.exception.EmployeeUniqueViolationException;
import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.InvalidPasswordException;
import br.com.threadstech.stockfy.exception.ProductUniqueViolationException;
import br.com.threadstech.stockfy.exception.UnavailableFromRefundException;
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

// TODO: Separar os handlers em classes diferentes e criar métodos base para construir cada método
//       sem repetição de código

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ApiExceptionHandler {

  private final MessageSource messageSource;
  private static final Locale locale = LocaleContextHolder.getLocale();

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorMessage> methodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request, BindingResult result) {
    String message = messageSource.getMessage("validation.error.payload", null, locale);
    return ResponseEntity.badRequest()
        .body(new ErrorMessage(request, result, HttpStatus.BAD_REQUEST, message, messageSource));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorMessage> entityNotFoundException(
      EntityNotFoundException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getItemNotFound()};
    String message = messageSource.getMessage("exception.entityNotFoundException", params, locale);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, message));
  }

  @ExceptionHandler(UnavailableFromRefundException.class)
  public ResponseEntity<ErrorMessage> unavailableFromRefundException(
      UnavailableFromRefundException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getRefoundId()};
    String message =
        messageSource.getMessage("exception.unavailableFromRefundException", params, locale);
    return ResponseEntity.unprocessableContent()
        .body(new ErrorMessage(request, HttpStatus.UNPROCESSABLE_CONTENT, message));
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ErrorMessage> invalidPasswordException(
      InvalidPasswordException ex, HttpServletRequest request) {
    String message =
        messageSource.getMessage(
            "exception.invalidPasswordException." + ex.getPasswordKey(), new Object[] {}, locale);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(new ErrorMessage(request, HttpStatus.UNPROCESSABLE_CONTENT, message));
  }

  // -- Conflict Handlers -- //

  @ExceptionHandler(ProductUniqueViolationException.class)
  public ResponseEntity<ErrorMessage> productUniqueFieldViolationException(
      ProductUniqueViolationException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getFieldName()};
    String message =
        messageSource.getMessage("exception.productUniqueViolationException", params, locale);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, message));
  }

  @ExceptionHandler(CustomerUniqueViolationException.class)
  public ResponseEntity<ErrorMessage> customerUniqueFieldViolationException(
      CustomerUniqueViolationException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getFieldName()};
    String message =
        messageSource.getMessage("exception.customerUniqueViolationException", params, locale);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, message));
  }

  @ExceptionHandler(EmployeeUniqueViolationException.class)
  public ResponseEntity<ErrorMessage> employeeUniqueFieldViolationException(
      EmployeeUniqueViolationException ex, HttpServletRequest request) {
    var params = new Object[] {ex.getFieldName()};
    String message =
        messageSource.getMessage("exception.employeeUniqueViolationException", params, locale);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, message));
  }
}
