package com.tattoo.scheduler.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a person's age meets the minimum requirement.
 * <p>
 * Usage: {@code @AgeValidator(min = 18)} on a {@link java.time.LocalDate} field.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidatorImpl.class)
public @interface AgeValidator {
    String message() default "Age requirement not met";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Minimum required age in years. */
    int min() default 0;
}
