package br.com.threadstech.stockfy.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

// TODO: Melhorar o método de validação. Usar valueOf
public class ProductTypeValidator implements ConstraintValidator<ProductType, String> {
  @Override
  public void initialize(ProductType constraintAnnotation) {
    // Nenhuma implementação necessária
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraint) {
    if (value == null || value.isEmpty()) {
      return true;
    }
    return Arrays.stream(br.com.threadstech.stockfy.enums.ProductType.values())
        .anyMatch(type -> type.name().equals(value));
  }
}
