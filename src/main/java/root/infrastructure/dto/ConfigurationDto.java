package root.infrastructure.dto;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import root.application.model.ProgressionType;
import root.infrastructure.dto.validation.TimeRangeValidation;

// todo: check validation via functional tests
@TimeRangeValidation
@Builder
public record ConfigurationDto(
		@Positive
		long startTimestamp,
		@Positive
		long endTimestamp,
		@NotEmpty
		Map<String, @NotEmpty Map<ProgressionType, @Valid ProgressionConfigurationDto>> segmentedProgressionsConfiguration
) {
}
