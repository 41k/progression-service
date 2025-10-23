package root.infrastructure.dto;

import jakarta.validation.constraints.Positive;

public record RewardDto(
		@Positive
		int unitId,
		@Positive
		int amount
) {
}
