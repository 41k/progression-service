package root.application.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.event.Event;
import root.application.model.UserState;
import root.application.service.progression_handler.ProgressionHandler;
import root.configuration.properties.ProgressionProperties;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgressionService {

	private final ProgressionProperties progressionProperties;
	private final Map<String, ProgressionHandler> progressionHandlers;
	private final UserStateService userStateService;
	private final RewardService rewardService;

	public void process(Event event) {
		try {
			var progressionHandlers = getEligibleProgressionHandlers(event);
			if (progressionHandlers.isEmpty()) {
				log.debug("No eligible progression handlers for {}", event);
				return;
			}
			process(event, progressionHandlers);
		} catch (Exception e) {
			log.error("Failed to process {}", event, e);
		}
	}

	private List<ProgressionHandler> getEligibleProgressionHandlers(Event event) {
		return progressionProperties.getEventSourceProperties(event.getSource()).progressionHandlers()
				.stream()
				.map(progressionHandlers::get)
				.filter(Objects::nonNull)
				.toList();
	}

	private void process(Event event, List<ProgressionHandler> progressionHandlers) {
		var userId = event.getUserId();
		var updatedUserState = userStateService.updateUserStateIfPresent(
				userId,
				userState -> makeProgression(event, userState, progressionHandlers)
		);
		updatedUserState.ifPresentOrElse(rewardService::sendRewards,
				() -> log.debug("User state is not found for userId={}, skipping {}", userId, event));

	}

	private UserState makeProgression(Event event,
	                                  UserState userState,
	                                  List<ProgressionHandler> progressionHandlers) {
		var updatedUserState = userState;
		for (var progressionHandler : progressionHandlers) {
			try {
				updatedUserState = progressionHandler.handle(event, updatedUserState);
			} catch (Exception e) {
				log.error("{} failed to handle {}", progressionHandler.getName(), event, e);
			}
		}
		return updatedUserState;
	}
}
