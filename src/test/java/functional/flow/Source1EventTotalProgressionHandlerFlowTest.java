package functional.flow;

import static functional.FunctionalTestData.SOURCE_1_EVENT;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userStateWithNewActiveConfiguration;
import static root.application.model.ProgressionType.SOURCE_1_TOTAL;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import functional.FunctionalTest;

public class Source1EventTotalProgressionHandlerFlowTest extends FunctionalTest {

	@Value("${progression.sources.SOURCE_1.topic}")
	private String source1Topic;

	@Test
	void shouldHandleProgressionSuccessfully() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithNewActiveConfiguration(0));

		// and
		assertUserProgression(SOURCE_1_TOTAL, null);

		// when
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT);
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT);
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT);
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT);

		// then
		assertUserProgressionWithPolling(SOURCE_1_TOTAL, 4);

		// when
		sendKafkaMessage(source1Topic, SOURCE_1_EVENT);

		// then
		assertUserProgressionWithPolling(SOURCE_1_TOTAL, 0);
	}
}
