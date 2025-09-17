package root.infrastructure.dto;

import lombok.Builder;

@Builder
public record ProgressionDto(
		long currentValue,
		long targetValue,
		RewardDto reward
) {
}
