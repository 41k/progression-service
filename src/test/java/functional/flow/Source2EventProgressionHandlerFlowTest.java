package functional.flow;

import static functional.FunctionalTestData.SEGMENT_1;
import static functional.FunctionalTestData.SOURCE_2_EVENT;
import static functional.FunctionalTestData.SOURCE_2_PROGRESSION_REWARD_MESSAGE;
import static functional.FunctionalTestData.USER_ID;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userStateWithActiveOutdatedConfiguration;
import static functional.FunctionalTestData.userStateWithNewActiveConfiguration;
import static root.application.model.ProgressionType.SOURCE_2;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import functional.FunctionalTest;

public class Source2EventProgressionHandlerFlowTest extends FunctionalTest {

	@Value("${progression.sources.SOURCE_2.topic}")
	private String source2Topic;

	@Test
	void shouldHandleProgressionSuccessfully() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithNewActiveConfiguration(0));

		// and
		assertUserProgression(SOURCE_2, null);

		// when
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);

		// then
		assertUserProgressionWithPolling(SOURCE_2, 200);

		// and
		rewardsTopicConsumer.assertNoMessageSent();

		// when
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);

		// then
		assertUserProgressionWithPolling(SOURCE_2, 0);

		// and
		rewardsTopicConsumer.assertMessageSent(Pair.of(USER_ID, SOURCE_2_PROGRESSION_REWARD_MESSAGE));
	}

	@Test
	void shouldHandleProgressionSuccessfully_withLatestConfigurationUpdatesSync() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithActiveOutdatedConfiguration());

		// and
		assertUserProgression(SOURCE_2, 150L);

		// and
		mockCallToSegmentationService(SEGMENT_1);

		// when
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);

		// then
		assertUserProgressionWithPolling(SOURCE_2, 200);

		// and
		rewardsTopicConsumer.assertNoMessageSent();

		// when
		sendKafkaMessage(source2Topic, SOURCE_2_EVENT);

		// then
		assertUserProgressionWithPolling(SOURCE_2, 0);

		// and
		rewardsTopicConsumer.assertMessageSent(Pair.of(USER_ID, SOURCE_2_PROGRESSION_REWARD_MESSAGE));
	}
}
