package root.infrastructure.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import root.infrastructure.dto.ConfigurationDto;

public class TimeRangeValidator implements ConstraintValidator<TimeRangeValidation, ConfigurationDto> {
	@Override
	public boolean isValid(ConfigurationDto dto, ConstraintValidatorContext context) {
		return dto.endTimestamp() > dto.startTimestamp();
	}
}
