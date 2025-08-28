package root.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import root.application.model.ProgressionConfiguration;

public record ProgressionConfigurationDto(
		@Min(1)
		long progressionTarget,
		@NotNull @Valid
		RewardDto reward
) {
	public ProgressionConfiguration toModel() {
		return new ProgressionConfiguration(progressionTarget, reward.toModel());
	}
}
