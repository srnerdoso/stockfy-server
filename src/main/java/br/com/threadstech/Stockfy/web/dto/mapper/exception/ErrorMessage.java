package br.com.threadstech.stockfy.web.dto.mapper.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Getter
@Setter
@ToString
public class ErrorMessage {

  public ErrorMessage() {}

  private String message;
  private String method;
  private String path;
  private int status;
  private String statusText;
  private Map<String, String> errors;

  public ErrorMessage(
      HttpServletRequest request, BindingResult result, HttpStatus status, String message, MessageSource messageSource) {
    this.message = message;
    this.method = request.getMethod();
    this.path = request.getRequestURI();
    this.status = status.value();
    this.statusText = status.getReasonPhrase();
    this.errors = new HashMap<>();
    addErrors(result, messageSource);
  }

  public ErrorMessage(
      HttpServletRequest request, HttpStatus status, String message, BindingResult result) {
    this.message = message;
    this.method = request.getMethod();
    this.path = request.getRequestURI();
    this.status = status.value();
    this.statusText = status.getReasonPhrase();
    this.errors = new HashMap<>();
    addErrors(result);
  }

  private void addErrors(BindingResult result) {
    this.errors = new HashMap<>();
    for (FieldError fieldError : result.getFieldErrors()) {
      this.errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
  }

  private void addErrors(BindingResult result, MessageSource messageSource) {
    this.errors = new HashMap<>();
    var locale = LocaleContextHolder.getLocale();
    for (FieldError fieldError : result.getFieldErrors()) {
      String code = fieldError.getCodes()[0];
      String message = messageSource.getMessage(code, fieldError.getArguments(), locale);
      this.errors.put(fieldError.getField(), message);
    }
  }
}
