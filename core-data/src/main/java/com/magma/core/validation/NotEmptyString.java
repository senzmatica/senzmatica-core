package com.magma.core.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmptyStringValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyString {
    String message() default "List cannot be empty or an empty string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
