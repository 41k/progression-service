package root.infrastructure.rewards;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import root.application.model.UserState;
import root.application.service.RewardService;

@Service
@RequiredArgsConstructor
public class AsyncRewardService implements RewardService {

	@Value("${rewards-topic}")
	private final String topic;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public void sendRewards(UserState userState) {
		var rewards = userState.getRewards();
		if (rewards.isEmpty()) {
			return;
		}
		var userId = userState.getUserId();
		var message = new RewardsMessage(userId, rewards);
		kafkaTemplate.send(topic, userId, message).join();
	}
}
