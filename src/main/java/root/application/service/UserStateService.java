package root.application.service;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.ProgressionsConfiguration;
import root.application.model.UserProgressionsConfiguration;
import root.application.model.UserState;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStateService {

	private final RetryTemplate optimisticLockRetryTemplate;
	private final UserStatePersistenceService userStatePersistenceService;
	private final ProgressionsConfigurationService configurationService;
	private final SegmentationService segmentationService;
	private final Clock clock;

	public Optional<UserState> findUserState(String userId) {
		return updateUserStateIfPresent(userId, Function.identity());
	}

	public Optional<UserState> updateUserStateIfPresent(String userId, Function<UserState, UserState> updateFunction) {
		return optimisticLockRetryTemplate.execute(context -> {
			if (context.getRetryCount() > 0) {
				// todo: check that logging is done properly
				log.warn("Optimistic lock happened during user state update for userId={}. Retrying, attempt {}", userId, context.getRetryCount());
			}
			return userStatePersistenceService.find(userId)
					.flatMap(this::syncUserStateWithLatestConfigurationUpdates)
					.map(updateFunction)
					.map(userStatePersistenceService::save);
		});
	}

	private Optional<UserState> syncUserStateWithLatestConfigurationUpdates(UserState userState) {
		var currentConfiguration = userState.getConfiguration();
		var segmentedConfiguration = configurationService.getCachedActiveConfigurationById(currentConfiguration.id());
		if (segmentedConfiguration == null) {
			return Optional.empty();
		}
		if (segmentationService.shouldReevaluateSegmentation(currentConfiguration.updateTimestamp())) {
			return reevaluateSegmentation(userState, segmentedConfiguration);
		}
		return Optional.of(userState);
	}

	private Optional<UserState> reevaluateSegmentation(UserState userState, ProgressionsConfiguration segmentedConfiguration) {
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
