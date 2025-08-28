package root.infrastructure.dto;

import jakarta.validation.constraints.Positive;
import root.application.model.Reward;

public record RewardDto(
		@Positive
		int unitId,
		@Positive
		int amount
) {
	public Reward toModel() {
		return new Reward(unitId, amount);
	}
}
