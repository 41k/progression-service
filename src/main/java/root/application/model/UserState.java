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
	private UserConfiguration configuration;
	@Builder.Default
	private Map<ProgressionType, Long> progressions = new HashMap<>();
	@Builder.Default
	private List<Reward> rewards = new ArrayList<>();
	private Long version;

	public ProgressionConfiguration getProgressionConfiguration(ProgressionType type) {
		return configuration.progressionsConfiguration().get(type);
	}

	public void addReward(Reward reward) {
		rewards.add(reward);
	}
}
