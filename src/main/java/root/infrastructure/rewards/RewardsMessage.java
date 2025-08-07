package root.infrastructure.rewards;

import java.util.List;

import root.application.model.Reward;

public record RewardsMessage(
		String userId,
		List<Reward> rewards
) {
}
