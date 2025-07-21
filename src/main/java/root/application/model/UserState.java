package root.application.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class UserState {

	private String userId;
	private UserProgressionsConfiguration configuration;
	@Builder.Default
	private Map<String, Long> progressions = new HashMap<>();
	@Builder.Default
	private List<Reward> rewards = new ArrayList<>();
	private Long version;

	public void incrementProgression(String progressionKey, Long progressionIncrement) {
		progressions.merge(progressionKey, progressionIncrement, Long::sum);
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}
}
