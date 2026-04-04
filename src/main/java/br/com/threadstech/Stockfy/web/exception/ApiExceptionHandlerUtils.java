package br.com.threadstech.stockfy.web.exception;

import java.util.Arrays;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class ApiExceptionHandlerUtils {

  public static String getExpectedTypeForMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    var requiredType = ex.getRequiredType();

    if (requiredType != null && requiredType.isEnum()) {
      return Arrays.stream(requiredType.getEnumConstants()).toList().toString();
    }
    return requiredType != null ? requiredType.getSimpleName() : "Unknown";
  }
}
