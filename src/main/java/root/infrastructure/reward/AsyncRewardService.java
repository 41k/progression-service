package root.infrastructure.reward;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import root.application.model.Reward;
import root.application.service.RewardService;

@Service
@RequiredArgsConstructor
public class AsyncRewardService implements RewardService {

	@Value("${rewards-topic}")
	private final String topic;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Override
	public void sendRewards(String userId, List<Reward> rewards) {
		if (rewards.isEmpty()) {
			return;
		}
		var message = new RewardsMessage(userId, rewards);
		kafkaTemplate.send(topic, userId, message).join();
	}
}
