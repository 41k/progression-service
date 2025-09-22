package functional.flow;

import static functional.FunctionalTestData.LOGIN_EVENT;
import static functional.FunctionalTestData.SEGMENT_1;
import static functional.FunctionalTestData.USER_ID;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userStateWithActiveConfiguration;
import static functional.FunctionalTestData.userStateWithInactiveConfiguration;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
		userStateRepository.save(userStateWithInactiveConfiguration());

		// and
		mockCallToSegmentationService(SEGMENT_1);

		// when
		sendKafkaMessage(loginTopic, LOGIN_EVENT);

		// then
		awaitAndAssert(() -> {
			var userState = userStateRepository.findById(USER_ID).get();
			assertThat(userState).isEqualTo(userStateWithActiveConfiguration());
		});
	}

	@Test
	void shouldCreateUserStateWithActiveConfiguration_ifUserIsNew() {
		// todo
	}
}
