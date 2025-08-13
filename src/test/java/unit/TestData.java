package unit;

import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static root.application.model.ProgressionType.SOURCE_1_WON;

import java.util.Map;
import java.util.Set;

import lombok.experimental.UtilityClass;
import root.application.model.ProgressionConfiguration;
import root.application.model.ProgressionType;
import root.application.model.ProgressionsConfiguration;
import root.application.model.Reward;
import root.application.model.UserProgressionsConfiguration;
import root.application.model.UserState;
import root.application.model.event.Event;
import root.application.model.event.Source1Event;

@UtilityClass
public class TestData {

	public static final String USER_ID = "1000";
	public static final Long CONFIGURATION_ID = 1L;
	public static final Long CONFIGURATION_UPDATE_TIMESTAMP = 1000L;
	public static final Event EVENT = Source1Event.builder().userId(Long.parseLong(USER_ID)).build();
	public static final String SEGMENT_1 = "segment-1";
	public static final String SEGMENT_2 = "segment-2";
	public static final Set<String> SEGMENTS = Set.of(SEGMENT_1, SEGMENT_2);
	public static final Reward REWARD_1 = new Reward(1, 100);
	public static final Reward REWARD_2 = new Reward(1, 200);
	public static final Map<ProgressionType, ProgressionConfiguration> PROGRESSIONS_CONFIGURATION_1 = Map.of(
			SOURCE_1_TOTAL, new ProgressionConfiguration(10, REWARD_1),
			SOURCE_1_WON, new ProgressionConfiguration(5, REWARD_1)
	);
	public static final Map<ProgressionType, ProgressionConfiguration> PROGRESSIONS_CONFIGURATION_2 = Map.of(
			SOURCE_1_TOTAL, new ProgressionConfiguration(15, REWARD_2),
			SOURCE_1_WON, new ProgressionConfiguration(8, REWARD_2)
	);
	public static final ProgressionsConfiguration SEGMENTED_PROGRESSIONS_CONFIGURATION =
			ProgressionsConfiguration.builder()
					.id(CONFIGURATION_ID)
					.updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP)
					.segmentedProgressionsConfiguration(Map.of(
							SEGMENT_1, PROGRESSIONS_CONFIGURATION_1,
							SEGMENT_2, PROGRESSIONS_CONFIGURATION_2
					))
					.build();
	public static final UserState USER_STATE = UserState.builder()
			.userId(USER_ID)
			.configuration(
					UserProgressionsConfiguration.builder()
							.id(CONFIGURATION_ID)
							.updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP)
							.progressionsConfiguration(PROGRESSIONS_CONFIGURATION_1)
							.build())
			.build();
}
