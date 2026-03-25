package br.com.threadstech.stockfy.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BarcodeValidator implements ConstraintValidator<Barcode, String> {
  @Override
  public void initialize(Barcode constraintAnnotation) {
    // Nenhuma implementação necessária
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }
    return value.matches("^\\d{7,20}$");
  }
}
