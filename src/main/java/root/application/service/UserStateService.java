package root.application.service;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.Configuration;
import root.application.model.UserConfiguration;
import root.application.model.UserState;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStateService {

	private final RetryTemplate optimisticLockRetryTemplate;
	private final UserStatePersistenceService userStatePersistenceService;
	private final ConfigurationService configurationService;
	private final SegmentationService segmentationService;
	private final Clock clock;

	// todo: public API for progressions retrieval
	// todo: public API for thresholds retrieval
	public Optional<UserState> findUserState(String userId) {
		return userStatePersistenceService.find(userId);
	}

	public Optional<UserState> updateUserStateIfPresent(String userId, Function<UserState, UserState> updateFunction) {
		return optimisticLockRetryTemplate.execute(context -> {
			if (context.getRetryCount() > 0) {
				log.warn("Optimistic lock happened during user state update for userId={}. Retry {}", userId, context.getRetryCount());
			}
			return userStatePersistenceService.find(userId)
					.map(this::syncUserStateWithLatestConfigurationUpdates)
					.map(updateFunction)
					.map(userStatePersistenceService::save);
		});
	}

	private UserState syncUserStateWithLatestConfigurationUpdates(UserState userState) {
		var userConfiguration = userState.getConfiguration();
		var configurationId = userConfiguration.id();
		var segmentedConfiguration = configurationService.getCachedActiveConfigurationById(configurationId);
		if (segmentedConfiguration == null) {
			log.debug("Configuration with id={} is no longer active/exist for user with id={}", configurationId, userState.getUserId());
			return null;
		}
		if (segmentationService.shouldReevaluateSegmentation(userConfiguration.updateTimestamp())) {
			return reevaluateSegmentation(userState, segmentedConfiguration);
		}
		return userState;
	}

	private UserState reevaluateSegmentation(UserState userState, Configuration segmentedConfiguration) {
		var userId = userState.getUserId();
		var segments = segmentedConfiguration.getAllSegments();
		var userSegment = segmentationService.evaluate(userId, segments);
		if (userSegment == null) {
			log.debug("Configuration with id={} is not applicable anymore for user with id={}", segmentedConfiguration.id(), userId);
			return null;
		}
		var userConfiguration = UserConfiguration.builder()
				.id(segmentedConfiguration.id())
				.updateTimestamp(clock.millis())
				.progressionsConfiguration(segmentedConfiguration.getUserProgressionsConfiguration(userSegment))
				.build();
		return userState.toBuilder().configuration(userConfiguration).build();
	}
}
