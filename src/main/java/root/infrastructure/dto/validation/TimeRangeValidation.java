package root.infrastructure.dto.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
public @interface TimeRangeValidation {
	String message() default "Time range is invalid: endTimestamp <= startTimestamp";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
