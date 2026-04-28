package com.tfg.schooledule.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;
    if (value.isBlank()) return false;
    if (value.length() < 8) return false;
    if (!value.matches(".*[A-Z].*")) return false;
    if (!value.matches(".*[a-z].*")) return false;
    if (!value.matches(".*[0-9].*")) return false;
    return true;
  }
}
