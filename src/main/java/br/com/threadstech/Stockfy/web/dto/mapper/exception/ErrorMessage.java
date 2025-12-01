package br.com.threadstech.stockfy.web.dto.mapper.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ErrorMessage {

  public ErrorMessage() {
  }

  private String message;
  private String method;
  private String path;
  private int status;
  private String statusText;
  private Map<String, String> errors;

  public ErrorMessage(HttpServletRequest request, HttpStatus status, String message) {
    this.message = message;
    this.method = request.getMethod();
    this.path = request.getRequestURI();
    this.status = status.value();
    this.statusText = status.getReasonPhrase();
    this.errors = new HashMap<>();
  }

  public ErrorMessage(HttpServletRequest request, HttpStatus status, String message, BindingResult result) {
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
}
