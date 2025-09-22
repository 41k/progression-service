package functional.flow;

import static functional.FunctionalTestData.LOGIN_EVENT;
import static functional.FunctionalTestData.SEGMENT_1;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userStateWithInactiveConfiguration;
import static functional.FunctionalTestData.userStateWithNewActiveConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import functional.FunctionalTest;

public class ActiveConfigurationAssignmentFlowTest extends FunctionalTest {

	@Value("${login-topic}")
	private String loginTopic;

	@Test
	void shouldAssignActivateConfigurationToUser() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithInactiveConfiguration(0));

		// and
		mockCallToSegmentationService(SEGMENT_1);

		// when
		sendKafkaMessage(loginTopic, LOGIN_EVENT);

		// then
		assertUserStateWithPolling(userStateWithNewActiveConfiguration(2));
	}

	@Test
	void shouldCreateUserStateWithActiveConfiguration_ifUserIsNew() {
		// given
		assertThat(userStateRepository.findAll()).isEmpty();

		// and
		configurationRepository.saveAndFlush(configurationEntity());

		// and
		mockCallToSegmentationService(SEGMENT_1);

		// when
		sendKafkaMessage(loginTopic, LOGIN_EVENT);

		// then
		assertUserStateWithPolling(userStateWithNewActiveConfiguration(1));
	}

	@Test
	void shouldSkipLoginEvent_ifThereIsNoActiveConfiguration() {
		// given
		userStateRepository.save(userStateWithInactiveConfiguration(0));

		// when
		sendKafkaMessage(loginTopic, LOGIN_EVENT);

		// then
		waitAndAssertUserState(userStateWithInactiveConfiguration(1));
	}

	@Test
	void shouldSkipLoginEvent_ifUserAlreadyHasThisConfiguration() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithNewActiveConfiguration(0));

		// when
		sendKafkaMessage(loginTopic, LOGIN_EVENT);

		// then
		waitAndAssertUserState(userStateWithNewActiveConfiguration(1));
	}

	@Test
	void shouldNotAssignActivateConfigurationToUser_ifItIsNotApplicableAccordingToSegmentation() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userStateWithInactiveConfiguration(0));

		// and
		mockCallToSegmentationService(null);

		// when
		sendKafkaMessage(loginTopic, LOGIN_EVENT);

		// then
		waitAndAssertUserState(userStateWithInactiveConfiguration(1));
	}
}
