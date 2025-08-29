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
	public static ProgressionConfigurationDto fromModel(ProgressionConfiguration configuration) {
		return new ProgressionConfigurationDto(
				configuration.progressionTarget(),
				RewardDto.fromModel(configuration.reward())
		);
	}

	public ProgressionConfiguration toModel() {
		return new ProgressionConfiguration(progressionTarget, reward.toModel());
	}
}
