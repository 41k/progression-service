package root.infrastructure.dto.validation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegexPatterns {

	public static final String ORDER = "(?i)^asc|desc$";
}
