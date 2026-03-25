package br.com.threadstech.stockfy.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class UnitTypeValidator implements ConstraintValidator<UnitType, String> {
  @Override
  public void initialize(UnitType constraintAnnotation) {
    // Nenhuma implementação necessária
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraint) {
    if (value == null || value.isEmpty()) {
      return true;
    }
    return Arrays.stream(br.com.threadstech.stockfy.core.enums.UnitType.values())
        .anyMatch(type -> type.name().equals(value));
  }
}
