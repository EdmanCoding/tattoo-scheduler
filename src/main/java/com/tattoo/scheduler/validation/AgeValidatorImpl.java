package com.tattoo.scheduler.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidatorImpl implements ConstraintValidator<AgeValidator, LocalDate> {
    private int minAge;

    @Override
    public void initialize(AgeValidator constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) return true; // Let @NotNull handle this
        return Period.between(birthDate, LocalDate.now()).getYears() >= minAge;
    }
}
