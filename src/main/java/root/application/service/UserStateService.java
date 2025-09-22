package root.application.service;

import java.time.Clock;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.Configuration;
import root.application.model.UserConfiguration;
import root.application.model.UserState;

// todo: change structure of "integration" layer packages to
// http.inbound, http.outbound
// messaging.consumer, messaging.producer
// persistence

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStateService {

	private final RetryTemplate optimisticLockRetryTemplate;
	private final UserStatePersistenceService userStatePersistenceService;
	private final ConfigurationService configurationService;
	private final SegmentationService segmentationService;
	private final Clock clock;

	public void assignActivateConfigurationToUser(String userId) {
		optimisticLockRetryTemplate.execute(context -> {
			var activeConfiguration = configurationService.getActiveConfiguration();
			if (activeConfiguration == null) {
				log.debug("There is no active configuration at the moment");
				return null;
			}
			if (context.getRetryCount() > 0) {
				log.warn("Optimistic lock happened during configuration[{}] assignment for user[{}]. Retry {}", activeConfiguration.id(), userId, context.getRetryCount());
			}
			userStatePersistenceService.find(userId).ifPresentOrElse(
					userState -> assignConfigurationToUser(userState, activeConfiguration),
					() -> createUserState(userId, activeConfiguration)
			);
			return null;
		});
	}

	public UserState findActiveUserState(String userId) {
		return userStatePersistenceService.find(userId)
				.filter(this::hasActiveConfiguration)
				.orElseThrow(() -> new NoSuchElementException("Active user state is not found by id=" + userId));
	}

	public Optional<UserState> updateUserStateIfPresent(String userId, Function<UserState, UserState> updateFunction) {
		return optimisticLockRetryTemplate.execute(context -> {
			if (context.getRetryCount() > 0) {
				log.warn("Optimistic lock happened during user[{}] state update. Retry {}", userId, context.getRetryCount());
			}
			return userStatePersistenceService.find(userId)
					.map(this::syncUserStateWithLatestConfigurationUpdates)
					.map(updateFunction)
					.map(userStatePersistenceService::save);
		});
	}

	private UserState syncUserStateWithLatestConfigurationUpdates(UserState userState) {
		var userId = userState.getUserId();
		var existingUserConfiguration = userState.getConfiguration();
		var configurationId = existingUserConfiguration.id();
		var segmentedConfiguration = configurationService.getActiveConfigurationById(configurationId);
		if (segmentedConfiguration == null) {
			log.debug("Configuration[{}] is no longer active/exist for user[{}]", configurationId, userId);
			return null;
		}
		if (segmentationService.shouldReevaluateSegmentation(existingUserConfiguration.updateTimestamp())) {
			return segmentConfiguration(userId, segmentedConfiguration)
					.map(newUserConfiguration -> userState.toBuilder().configuration(newUserConfiguration).build())
					.orElse(null);
		}
		return userState;
	}

	private Optional<UserConfiguration> segmentConfiguration(String userId, Configuration segmentedConfiguration) {
		var segments = segmentedConfiguration.getAllSegments();
		var userSegment = segmentationService.evaluate(userId, segments);
		if (userSegment == null) {
			log.debug("Configuration[{}] is not applicable for user[{}]", segmentedConfiguration.id(), userId);
			return Optional.empty();
		}
		return Optional.of(
				UserConfiguration.builder()
						.id(segmentedConfiguration.id())
						.updateTimestamp(clock.millis())
						.progressionsConfiguration(segmentedConfiguration.getUserProgressionsConfiguration(userSegment))
						.build());
	}

	private boolean hasActiveConfiguration(UserState userState) {
		var configurationId = userState.getConfiguration().id();
		var configuration = configurationService.getActiveConfigurationById(configurationId);
		return configuration != null;
	}

	private void createUserState(String userId, Configuration configuration) {
		var userState = UserState.builder().userId(userId).build();
		assignConfigurationToUser(userState, configuration);
	}

	private void assignConfigurationToUser(UserState userState, Configuration configuration) {
		var userId = userState.getUserId();
		var configurationId = configuration.id();
		var userHasSameConfiguration = Optional.ofNullable(userState.getConfiguration())
				.filter(currentConfiguration -> currentConfiguration.id() == configurationId)
				.isPresent();
		if (userHasSameConfiguration) {
			log.debug("Configuration[{}] is already assigned to user[{}]", configurationId, userId);
			return;
		}
		segmentConfiguration(userId, configuration).ifPresent(userConfiguration -> {
			var userStateWithNewConfiguration = UserState.builder()
					.userId(userId)
					.configuration(userConfiguration)
					.version(userState.getVersion())
					.build();
			userStatePersistenceService.save(userStateWithNewConfiguration);
		});
	}
}
