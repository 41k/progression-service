package functional.flow;

import static functional.FunctionalTestData.SEGMENT_1;
import static functional.FunctionalTestData.SOURCE_1_EVENT_FOR_WON_PROGRESSION;
import static functional.FunctionalTestData.SOURCE_1_PROGRESSION_REWARD_MESSAGE;
import static functional.FunctionalTestData.USER_ID;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userStateWithActiveOutdatedConfiguration;
import static functional.FunctionalTestData.userStateWithNewActiveConfiguration;
import static root.application.model.ProgressionType.SOURCE_1_WON;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import functional.FunctionalTest;

public class Source1EventWonProgressionHandlerFlowTest extends FunctionalTest {

	@Value("${progression.sources.SOURCE_1.topic}")
	private String source1Topic;

	@Test
	void shouldHandleProgressionSuccessfully() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithNewActiveConfiguration(0));

		// and
		assertUserProgression(SOURCE_1_WON, null);

		// when
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT_FOR_WON_PROGRESSION);
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT_FOR_WON_PROGRESSION);

		// then
		assertUserProgressionWithPolling(SOURCE_1_WON, 2);

		// and
		rewardsTopicConsumer.assertNoMessageSent();

		// when
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT_FOR_WON_PROGRESSION);

		// then
		assertUserProgressionWithPolling(SOURCE_1_WON, 0);

		// and
		rewardsTopicConsumer.assertMessageSent(Pair.of(USER_ID, SOURCE_1_PROGRESSION_REWARD_MESSAGE));
	}

	@Test
	void shouldHandleProgressionSuccessfully_withLatestConfigurationUpdatesSync() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithActiveOutdatedConfiguration());

		// and
		assertUserProgression(SOURCE_1_WON, 1L);

		// and
		mockCallToSegmentationService(SEGMENT_1);

		// when
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT_FOR_WON_PROGRESSION);

		// then
		assertUserProgressionWithPolling(SOURCE_1_WON, 2);

		// and
		rewardsTopicConsumer.assertNoMessageSent();

		// when
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT_FOR_WON_PROGRESSION);

		// then
		assertUserProgressionWithPolling(SOURCE_1_WON, 0);

		// and
		rewardsTopicConsumer.assertMessageSent(Pair.of(USER_ID, SOURCE_1_PROGRESSION_REWARD_MESSAGE));
	}
}
