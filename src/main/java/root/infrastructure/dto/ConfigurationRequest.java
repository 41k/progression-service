package root.infrastructure.dto;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import root.application.model.ProgressionType;
import root.infrastructure.dto.validation.TimeRangeValidation;

@TimeRangeValidation
@Builder(toBuilder = true)
public record ConfigurationRequest(
		@Positive
		long startTimestamp,
		@Positive
		long endTimestamp,
		@NotEmpty
		Map<@NotBlank String, @NotEmpty Map<ProgressionType, @NotNull @Valid ProgressionConfigurationDto>> segmentedProgressionsConfiguration
) {
}
