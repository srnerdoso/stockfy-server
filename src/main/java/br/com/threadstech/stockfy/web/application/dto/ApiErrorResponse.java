package br.com.threadstech.stockfy.web.application.dto;

import java.util.List;
import lombok.Getter;
import org.springframework.validation.FieldError;

@Getter
public class ApiErrorResponse {
  private final String type;
  private final String title;
  private final int status;
  private final String detail;
  private final String instance;
  private final List<ErrorDetail> fieldErrors;

  public ApiErrorResponse(String type, String title, int status, String detail, String instance) {
    this(type, title, status, detail, instance, null);
  }

  public ApiErrorResponse(
      String type,
      String title,
      int status,
      String detail,
      String instance,
      List<FieldError> fieldErrors) {
    this.type = type;
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.instance = instance;
    this.fieldErrors =
        fieldErrors != null
            ? fieldErrors.stream()
                .map(e -> new ErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList()
            : null;
  }

  public record ErrorDetail(String field, String message) {}
}
