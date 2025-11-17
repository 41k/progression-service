package unit.application.service.progression.handler;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static root.application.model.ProgressionType.SOURCE_1_WON;
import static unit.UnitTestData.REWARD_1;
import static unit.UnitTestData.USER_CONFIGURATION;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import root.application.model.UserConfiguration;
import root.application.model.UserState;
import root.application.model.event.Event;
import root.application.model.event.Source1Event;
import root.application.service.progression.handler.Source1EventWonProgressionHandler;

public class Source1EventWonProgressionHandlerTest {

	private static final Source1EventWonProgressionHandler HANDLER = new Source1EventWonProgressionHandler();

	@ParameterizedTest
	@MethodSource("eligibilityCheckParams")
	void shouldCheckEventEligibility(Event event, Boolean result) {
		assertThat(HANDLER.isEligible(event)).isEqualTo(result);
	}

	@Test
	void handle_shouldIncreaseProgression_ifProgressionThresholdIsNotReached() {
		// given
		var event = new Source1Event();
		var userState = UserState.builder()
				.configuration(USER_CONFIGURATION)
				.progressions(new HashMap<>() {{
					put(SOURCE_1_TOTAL, 7L);
					put(SOURCE_1_WON, 3L);
				}})
				.build();

		// when
		var reward = HANDLER.handle(event, userState);

		// then
		assertThat(reward.isEmpty()).isTrue();
		assertThat(userState.getProgressions()).isEqualTo(Map.of(
				SOURCE_1_TOTAL, 7L,
				SOURCE_1_WON, 4L
		));
	}

	@Test
	void handle_shouldResetProgressionToZero_andReturnReward_ifProgressionThresholdIsReached() {
		// given
		var event = new Source1Event();
		var userState = UserState.builder()
				.configuration(USER_CONFIGURATION)
				.progressions(new HashMap<>() {{
					put(SOURCE_1_TOTAL, 6L);
					put(SOURCE_1_WON, 4L);
				}})
				.build();

		// when
		var reward = HANDLER.handle(event, userState).orElseThrow();

		// then
		assertThat(reward).isEqualTo(REWARD_1);
		assertThat(userState.getProgressions()).isEqualTo(Map.of(
				SOURCE_1_TOTAL, 6L,
				SOURCE_1_WON, 0L
		));
	}

	@Test
	void handle_shouldNotIncreaseProgression_ifProgressionConfigurationIsMissed() {
		// given
		var event = new Source1Event();
		var userState = UserState.builder()
				.configuration(
						UserConfiguration.builder().progressionsConfiguration(Map.of()).build()
				)
				.progressions(new HashMap<>() {{
					put(SOURCE_1_TOTAL, 4L);
					put(SOURCE_1_WON, 1L);
				}})
				.build();

		// when
		var reward = HANDLER.handle(event, userState);

		// then
		assertThat(reward.isEmpty()).isTrue();
		assertThat(userState.getProgressions()).isEqualTo(Map.of(
				SOURCE_1_TOTAL, 4L,
				SOURCE_1_WON, 1L
		));
	}

	private static Stream<Arguments> eligibilityCheckParams() {
		return Stream.of(
				Arguments.of(Source1Event.builder().userId(null).build(), FALSE),
				Arguments.of(Source1Event.builder().userId(1L).result(null).build(), FALSE),
				Arguments.of(Source1Event.builder().userId(1L).result("WIN").build(), FALSE),
				Arguments.of(Source1Event.builder().userId(1L).result("WON").build(), TRUE)
		);
	}
}
