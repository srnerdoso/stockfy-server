package br.com.threadstech.stockfy.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<Phone, String> {

  private String region;

  @Override
  public void initialize(Phone constraintAnnotation) {
    this.region = constraintAnnotation.region();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isEmpty()) {
      return true;
    }
    if (!value.matches("^[+()\\d\\s-]+$")) {
      return false;
    }
    if (!value.startsWith("+")) {
      return false;
    }
    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    try {
      return phoneUtil.isValidNumber(phoneUtil.parse(value, region));
    } catch (NumberParseException ex) {
      return false;
    }
  }
}
