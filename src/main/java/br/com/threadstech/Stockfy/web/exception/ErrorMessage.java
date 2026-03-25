package br.com.threadstech.stockfy.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {

  private String message;
  private String method;
  private String path;
  private int status;
  private String statusText;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, String> errors;

  public ErrorMessage(
      HttpServletRequest request,
      BindingResult result,
      HttpStatus status,
      String message,
      MessageSource messageSource) {
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

  public ErrorMessage(HttpServletRequest request, HttpStatus status, String message) {
    this.message = message;
    this.method = request.getMethod();
    this.path = request.getRequestURI();
    this.status = status.value();
    this.statusText = status.getReasonPhrase();
    this.errors = new HashMap<>();
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
      String message = resolveMessage(fieldError, messageSource, locale);
      this.errors.put(fieldError.getField(), message);
    }
  }

  private String resolveMessage(FieldError fieldError, MessageSource messageSource, java.util.Locale locale) {
    String defaultMessage = fieldError.getDefaultMessage();
    
    // If the default message is a key like {NotBlank.productDto.name}, try to resolve it
    if (defaultMessage != null && defaultMessage.startsWith("{") && defaultMessage.endsWith("}")) {
      String key = defaultMessage.substring(1, defaultMessage.length() - 1);
      try {
        return messageSource.getMessage(key, fieldError.getArguments(), locale);
      } catch (org.springframework.context.NoSuchMessageException e) {
        // Fallback to searching through fieldError.getCodes()
      }
    }

    // Try standard Spring validation codes
    for (String code : fieldError.getCodes()) {
      try {
        return messageSource.getMessage(code, fieldError.getArguments(), locale);
      } catch (org.springframework.context.NoSuchMessageException e) {
        // Continue to next code
      }
    }

    return defaultMessage;
  }
}
