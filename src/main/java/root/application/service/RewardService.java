package root.application.service;

import java.util.List;

import root.application.model.Reward;

public interface RewardService {

	void sendRewards(String userId, List<Reward> rewards);
}
