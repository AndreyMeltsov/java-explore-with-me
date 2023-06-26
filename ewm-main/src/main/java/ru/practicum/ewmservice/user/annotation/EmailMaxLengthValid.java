package ru.practicum.ewmservice.user.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.TYPE_USE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = CheckEmailValidator.class)
public @interface EmailMaxLengthValid {
    String message() default "Local part and domain part should be less than 64 symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
