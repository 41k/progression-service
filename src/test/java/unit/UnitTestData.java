package unit;

import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static root.application.model.ProgressionType.SOURCE_1_WON;

import java.util.Map;
import java.util.Set;

import lombok.experimental.UtilityClass;
import root.application.model.Configuration;
import root.application.model.ProgressionConfiguration;
import root.application.model.ProgressionType;
import root.application.model.Reward;
import root.application.model.UserConfiguration;
import root.application.model.UserState;
import root.application.model.event.Event;
import root.application.model.event.Source1Event;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ProgressionConfigurationDto;
import root.infrastructure.dto.RewardDto;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.state.UserStateDocument;

@UtilityClass
public class UnitTestData {

	public static final String USER_ID = "1000";
	public static final Long CONFIGURATION_ID = 1L;
	public static final String CONFIGURATION_NAME = "configuration";
	public static final Long CONFIGURATION_START_TIMESTAMP = 500L;
	public static final Long CONFIGURATION_END_TIMESTAMP = 1500L;
	public static final Long CONFIGURATION_UPDATE_TIMESTAMP = 1000L;
	public static final Event EVENT = Source1Event.builder().userId(Long.parseLong(USER_ID)).build();
	public static final String SEGMENT_1 = "segment-1";
	public static final String SEGMENT_2 = "segment-2";
	public static final Set<String> SEGMENTS = Set.of(SEGMENT_1, SEGMENT_2);
	public static final RewardDto REWARD_DTO_1 = new RewardDto(1, 100);
	public static final RewardDto REWARD_DTO_2 = new RewardDto(1, 200);
	public static final Reward REWARD_1 = new Reward(1, 100);
	public static final Reward REWARD_2 = new Reward(1, 200);

	public static final Map<ProgressionType, ProgressionConfigurationDto> PROGRESSIONS_CONFIGURATION_DTO_1 = Map.of(
			SOURCE_1_TOTAL, new ProgressionConfigurationDto(10, REWARD_DTO_1),
			SOURCE_1_WON, new ProgressionConfigurationDto(5, REWARD_DTO_1)
	);

	public static final Map<ProgressionType, ProgressionConfigurationDto> PROGRESSIONS_CONFIGURATION_DTO_2 = Map.of(
			SOURCE_1_TOTAL, new ProgressionConfigurationDto(15, REWARD_DTO_2),
			SOURCE_1_WON, new ProgressionConfigurationDto(8, REWARD_DTO_2)
	);

	public static final Map<ProgressionType, ProgressionConfiguration> PROGRESSIONS_CONFIGURATION_1 = Map.of(
			SOURCE_1_TOTAL, new ProgressionConfiguration(10, REWARD_1),
			SOURCE_1_WON, new ProgressionConfiguration(5, REWARD_1)
	);

	public static final Map<ProgressionType, ProgressionConfiguration> PROGRESSIONS_CONFIGURATION_2 = Map.of(
			SOURCE_1_TOTAL, new ProgressionConfiguration(15, REWARD_2),
			SOURCE_1_WON, new ProgressionConfiguration(8, REWARD_2)
	);

	public static final Map<String, Map<ProgressionType, ProgressionConfigurationDto>> SEGMENTED_PROGRESSIONS_CONFIGURATION_DTO = Map.of(
			SEGMENT_1, PROGRESSIONS_CONFIGURATION_DTO_1,
			SEGMENT_2, PROGRESSIONS_CONFIGURATION_DTO_2
	);

	public static final Map<String, Map<ProgressionType, ProgressionConfiguration>> SEGMENTED_PROGRESSIONS_CONFIGURATION = Map.of(
			SEGMENT_1, PROGRESSIONS_CONFIGURATION_1,
			SEGMENT_2, PROGRESSIONS_CONFIGURATION_2
	);

	public static final ConfigurationRequest CONFIGURATION_REQUEST =
			ConfigurationRequest.builder()
					.name(CONFIGURATION_NAME)
					.startTimestamp(CONFIGURATION_START_TIMESTAMP)
					.endTimestamp(CONFIGURATION_END_TIMESTAMP)
					.segmentedProgressionsConfiguration(SEGMENTED_PROGRESSIONS_CONFIGURATION_DTO)
					.build();

	public static final Configuration CONFIGURATION =
			Configuration.builder()
					.id(CONFIGURATION_ID)
					.name(CONFIGURATION_NAME)
					.startTimestamp(CONFIGURATION_START_TIMESTAMP)
					.endTimestamp(CONFIGURATION_END_TIMESTAMP)
					.updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP)
					.segmentedProgressionsConfiguration(SEGMENTED_PROGRESSIONS_CONFIGURATION)
					.build();

	public static final ConfigurationEntity CONFIGURATION_ENTITY =
			ConfigurationEntity.builder()
					.id(CONFIGURATION_ID)
					.name(CONFIGURATION_NAME)
					.startTimestamp(CONFIGURATION_START_TIMESTAMP)
					.endTimestamp(CONFIGURATION_END_TIMESTAMP)
					.updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP)
					.segmentedProgressionsConfiguration(SEGMENTED_PROGRESSIONS_CONFIGURATION)
					.build();

	public static final UserConfiguration USER_CONFIGURATION = UserConfiguration.builder()
			.id(CONFIGURATION_ID)
			.updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP)
			.progressionsConfiguration(PROGRESSIONS_CONFIGURATION_1)
			.build();

	public static final Map<ProgressionType, Long> USER_PROGRESSIONS = Map.of(
			SOURCE_1_TOTAL, 5L,
			SOURCE_1_WON, 2L
	);

	public static final Long USER_STATE_VERSION = 3L;

	public static final UserState USER_STATE = UserState.builder()
			.userId(USER_ID)
			.configuration(USER_CONFIGURATION)
			.progressions(USER_PROGRESSIONS)
			.version(USER_STATE_VERSION)
			.build();

	public static final UserStateDocument USER_STATE_DOCUMENT = UserStateDocument.builder()
			.id(USER_ID)
			.configuration(USER_CONFIGURATION)
			.progressions(USER_PROGRESSIONS)
			.version(USER_STATE_VERSION)
			.build();
}
