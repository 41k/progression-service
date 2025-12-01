package unit.application.service.progression.handler;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static root.application.model.ProgressionType.SOURCE_1_WON;
import static root.application.model.ProgressionType.SOURCE_2;
import static unit.UnitTestData.REWARD_1;
import static unit.UnitTestData.USER_CONFIGURATION;
import static unit.UnitTestData.USER_ID;

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
import root.application.model.event.Source2Event;
import root.application.service.progression.handler.Source2EventProgressionHandler;

public class Source2EventProgressionHandlerTest {

	private static final Source2EventProgressionHandler HANDLER = new Source2EventProgressionHandler();
	private static final Source2Event EVENT = new Source2Event(USER_ID, 50);

	@ParameterizedTest
	@MethodSource("eligibilityCheckParams")
	void shouldCheckEventEligibility(Event event, Boolean result) {
		assertThat(HANDLER.isEligible(event)).isEqualTo(result);
	}

	@Test
	void handle_shouldIncreaseProgression_ifProgressionThresholdIsNotReached() {
		// given
		var userState = UserState.builder()
				.configuration(USER_CONFIGURATION)
				.progressions(new HashMap<>() {{
					put(SOURCE_1_TOTAL, 5L);
					put(SOURCE_2, 100L);
				}})
				.build();

		// when
		var reward = HANDLER.handle(EVENT, userState);

		// then
		assertThat(reward.isEmpty()).isTrue();
		assertThat(userState.getProgressions()).isEqualTo(Map.of(
				SOURCE_1_TOTAL, 5L,
				SOURCE_2, 150L
		));
	}

	@Test
	void handle_shouldResetProgressionToZero_andReturnReward_ifProgressionThresholdIsReached() {
		// given
		var userState = UserState.builder()
				.configuration(USER_CONFIGURATION)
				.progressions(new HashMap<>() {{
					put(SOURCE_2, 200L);
					put(SOURCE_1_WON, 7L);
				}})
				.build();

		// when
		var reward = HANDLER.handle(EVENT, userState).orElseThrow();

		// then
		assertThat(reward).isEqualTo(REWARD_1);
		assertThat(userState.getProgressions()).isEqualTo(Map.of(
				SOURCE_2, 0L,
				SOURCE_1_WON, 7L
		));
	}

	@Test
	void handle_shouldNotIncreaseProgression_ifProgressionConfigurationIsMissed() {
		// given
		var userState = UserState.builder()
				.configuration(
						UserConfiguration.builder().progressionsConfiguration(Map.of()).build()
				)
				.progressions(new HashMap<>() {{
					put(SOURCE_2, 60L);
					put(SOURCE_1_WON, 1L);
				}})
				.build();

		// when
		var reward = HANDLER.handle(EVENT, userState);

		// then
		assertThat(reward.isEmpty()).isTrue();
		assertThat(userState.getProgressions()).isEqualTo(Map.of(
				SOURCE_2, 60L,
				SOURCE_1_WON, 1L
		));
	}

	private static Stream<Arguments> eligibilityCheckParams() {
		return Stream.of(
				Arguments.of(Source2Event.builder().userId(null).build(), FALSE),
				Arguments.of(Source2Event.builder().userId("").build(), FALSE),
				Arguments.of(Source2Event.builder().userId("10").build(), TRUE)
		);
	}
}
