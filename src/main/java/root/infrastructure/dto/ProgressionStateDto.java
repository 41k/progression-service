package root.infrastructure.dto;

import lombok.Builder;
import root.application.model.Reward;

@Builder
public record ProgressionStateDto(
		long currentValue,
		long targetValue,
		Reward reward
) {
}
