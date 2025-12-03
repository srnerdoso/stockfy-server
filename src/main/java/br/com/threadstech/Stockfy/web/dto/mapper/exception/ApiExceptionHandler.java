package br.com.threadstech.stockfy.web.dto.mapper.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.threadstech.stockfy.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorMessage> entityNotFoundException(MethodArgumentNotValidException ex,
      HttpServletRequest request,
      BindingResult result) {
    log.error("Entity not found - {}", ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, ex.getMessage(), result));
  }
}
