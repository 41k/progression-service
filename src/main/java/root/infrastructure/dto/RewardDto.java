package root.infrastructure.dto;

import jakarta.validation.constraints.Positive;
import root.application.model.Reward;

public record RewardDto(
		@Positive
		int unitId,
		@Positive
		int amount
) {
	public static RewardDto fromModel(Reward reward) {
		return new RewardDto(reward.unitId(), reward.amount());
	}

	public Reward toModel() {
		return new Reward(unitId, amount);
	}
}
