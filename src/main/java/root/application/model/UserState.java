package root.application.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserState {

	private String userId;
	private UserProgressionsConfiguration progressionConfiguration;
	@Builder.Default
	private Map<String, Long> progressions = new HashMap<>();
	@Builder.Default
	private List<Reward> rewards = new ArrayList<>();

	public void incrementProgression(String progressionKey, Long progressionIncrement) {
		progressions.merge(progressionKey, progressionIncrement, Long::sum);
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}
}
