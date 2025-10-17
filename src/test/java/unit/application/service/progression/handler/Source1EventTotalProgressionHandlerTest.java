package unit.application.service.progression.handler;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static root.application.model.ProgressionType.SOURCE_1_WON;
import static unit.UnitTestData.REWARD_1;
import static unit.UnitTestData.REWARD_2;
import static unit.UnitTestData.USER_CONFIGURATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import root.application.service.progression.handler.Source1EventTotalProgressionHandler;

public class Source1EventTotalProgressionHandlerTest {

	private static final Source1EventTotalProgressionHandler HANDLER = new Source1EventTotalProgressionHandler();

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
					put(SOURCE_1_TOTAL, 5L);
					put(SOURCE_1_WON, 2L);
				}})
				.build();
		var expectedUserStateAfterEventHandling = userState.toBuilder()
				.progressions(Map.of(
						SOURCE_1_TOTAL, 6L,
						SOURCE_1_WON, 2L
				))
				.build();

		// when
		var userStateAfterEventHandling = HANDLER.handle(event, userState);

		// then
		assertThat(userStateAfterEventHandling).isEqualTo(expectedUserStateAfterEventHandling);
	}

	@Test
	void handle_shouldAddReward_andResetProgressionToZero_ifProgressionThresholdIsReached() {
		// given
		var event = new Source1Event();
		var userState = UserState.builder()
				.configuration(USER_CONFIGURATION)
				.progressions(new HashMap<>() {{
					put(SOURCE_1_TOTAL, 9L);
					put(SOURCE_1_WON, 7L);
				}})
				.rewards(new ArrayList<>() {{
					add(REWARD_2);
				}})
				.build();
		var expectedUserStateAfterEventHandling = userState.toBuilder()
				.progressions(Map.of(
						SOURCE_1_TOTAL, 0L,
						SOURCE_1_WON, 7L
				))
				.rewards(List.of(REWARD_2, REWARD_1))
				.build();

		// when
		var userStateAfterEventHandling = HANDLER.handle(event, userState);

		// then
		assertThat(userStateAfterEventHandling).isEqualTo(expectedUserStateAfterEventHandling);
	}

	@Test
	void handle_shouldNotIncreaseProgression_ifProgressionConfigurationIsMissed() {
		// given
		var event = new Source1Event();
		var userState = UserState.builder()
				.configuration(
						UserConfiguration.builder().progressionsConfiguration(Map.of()).build()
				)
				.progressions(Map.of(
						SOURCE_1_TOTAL, 4L,
						SOURCE_1_WON, 1L
				))
				.build();

		// when
		var userStateAfterEventHandling = HANDLER.handle(event, userState);

		// then
		assertThat(userStateAfterEventHandling).isEqualTo(userState);
	}

	private static Stream<Arguments> eligibilityCheckParams() {
		return Stream.of(
				Arguments.of(Source1Event.builder().userId(null).build(), FALSE),
				Arguments.of(Source1Event.builder().userId(1L).build(), TRUE)
		);
	}
}
