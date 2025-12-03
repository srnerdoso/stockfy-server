package br.com.threadstech.stockfy.web.dto.mapper.exception;

import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import br.com.threadstech.stockfy.exception.UniqueFieldViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
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

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorMessage> entityNotFoundException(
      MethodArgumentNotValidException ex, HttpServletRequest request, BindingResult result) {
    log.error("Entity not found - {}", ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, ex.getMessage(), result));
  }

  @ExceptionHandler(UniqueFieldViolationException.class)
  public ResponseEntity<ErrorMessage> uniqueFieldViolationException(
      MethodArgumentNotValidException ex, HttpServletRequest request, BindingResult result) {
    log.error("Unique field violation - {}", ex);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ErrorMessage(request, result, HttpStatus.CONFLICT, ex.getMessage(), messageSource));
  }
}
