package root.infrastructure.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import root.infrastructure.dto.ConfigurationRequest;

public class TimeRangeValidator implements ConstraintValidator<TimeRangeValidation, ConfigurationRequest> {
	@Override
	public boolean isValid(ConfigurationRequest request, ConstraintValidatorContext context) {
		return request.endTimestamp() > request.startTimestamp();
	}
}
