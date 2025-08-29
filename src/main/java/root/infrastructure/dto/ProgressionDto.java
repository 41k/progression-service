package root.infrastructure.dto;

import lombok.Builder;
import root.application.model.Reward;

@Builder
public record ProgressionDto(
		long currentValue,
		long targetValue,
		Reward reward
) {
}
