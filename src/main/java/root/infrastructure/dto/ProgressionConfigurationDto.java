package root.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder(toBuilder = true)
public record ProgressionConfigurationDto(
		@Min(1)
		long progressionTarget,
		@NotNull @Valid
		RewardDto reward
) {
}
