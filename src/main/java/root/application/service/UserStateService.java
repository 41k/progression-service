package root.application.service;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.UserState;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserStateService {

	private final RetryTemplate optimisticLockRetryTemplate;
	private final UserStatePersistenceService userStatePersistenceService;
	private final ConfigurationService configurationService;

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
		return configurationService.getUpdatedConfiguration(userState.getUserId(), userState.getConfiguration()) // todo refactor
				.map(updatedConfiguration -> userState.toBuilder().configuration(updatedConfiguration).build());
	}
}
