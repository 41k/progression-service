package root.application.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.ProgressionsConfiguration;
import root.application.model.UserProgressionsConfiguration;
import root.application.model.UserState;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigurationService {

	private final ConfigurationPersistenceService configurationPersistenceService;
	private final Clock clock;
	@Value("${segmentation-refresh-period-millis:300000}")
	private final long segmentationRefreshPeriodInMillis;
	private final SegmentationService segmentationService;

	public Optional<UserState> syncUserStateWithLatestConfigurationUpdates(UserState userState) {
		var currentConfiguration = userState.getConfiguration();
		var segmentedConfiguration = configurationPersistenceService.getCachedEnabledActiveConfigurationById(currentConfiguration.id());
		if (segmentedConfiguration == null) {
			return Optional.empty();
		}
		if (shouldRefreshSegmentation(currentConfiguration)) {
			return refreshSegmentation(userState, segmentedConfiguration);
		}
		return Optional.of(userState);
	}

	private boolean shouldRefreshSegmentation(UserProgressionsConfiguration currentConfiguration) {
		return clock.millis() - currentConfiguration.updateTimestamp() > segmentationRefreshPeriodInMillis;
	}

	private Optional<UserState> refreshSegmentation(UserState userState, ProgressionsConfiguration segmentedConfiguration) {
		var userId = userState.getUserId();
		var segments = segmentedConfiguration.getAllSegments();
		var userSegment = segmentationService.evaluate(userId, segments);
		if (userSegment == null) {
			log.debug("Configuration with id={} is not applicable anymore for user with id={}", segmentedConfiguration.id(), userId);
			return Optional.empty();
		}
		var userProgressionsConfiguration = UserProgressionsConfiguration.builder()
				.id(segmentedConfiguration.id())
				.updateTimestamp(clock.millis())
				.progressionsConfiguration(segmentedConfiguration.getUserProgressionsConfiguration(userSegment))
				.build();
		return Optional.of(userState.toBuilder().configuration(userProgressionsConfiguration).build());
	}
}
