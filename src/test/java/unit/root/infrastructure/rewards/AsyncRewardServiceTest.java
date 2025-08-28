package unit.root.infrastructure.rewards;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static unit.TestData.REWARD_1;
import static unit.TestData.REWARD_2;
import static unit.TestData.USER_ID;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import root.application.model.Reward;
import root.application.model.UserState;
import root.infrastructure.reward.AsyncRewardService;
import root.infrastructure.reward.RewardsMessage;

@ExtendWith(MockitoExtension.class)
public class AsyncRewardServiceTest {

	private static final String TOPIC = "topic";
	private static final List<Reward> REWARDS = List.of(REWARD_1, REWARD_2);

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;
	private AsyncRewardService asyncRewardService;

	@BeforeEach
	void setUp() {
		asyncRewardService = new AsyncRewardService(TOPIC, kafkaTemplate);
	}

	@Test
	void shouldSendRewards() {
		// given
		var userStateWithRewards = UserState.builder().userId(USER_ID).rewards(REWARDS).build();
		var message = new RewardsMessage(USER_ID, REWARDS);
		when(kafkaTemplate.send(TOPIC, USER_ID, message)).thenReturn(CompletableFuture.completedFuture(null));

		// when
		asyncRewardService.sendRewards(userStateWithRewards);

		// then
		verify(kafkaTemplate).send(TOPIC, USER_ID, message);
	}

	@Test
	void shouldNotSendRewards_ifThereAreNoRewardsToBeSent() {
		var userStateWithoutRewards = UserState.builder().build();

		asyncRewardService.sendRewards(userStateWithoutRewards);

		verify(kafkaTemplate, never()).send(any(), any(), any());
	}
}
