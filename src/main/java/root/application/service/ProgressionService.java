package root.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.ProgressionUpdateTask;
import root.application.model.event.Event;
import root.application.service.progression.handler.ProgressionHandler;
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
				.filter(progressionHandler -> progressionHandler != null && progressionHandler.isEligible(event))
				.toList();
	}

	private void process(Event event, List<ProgressionHandler> progressionHandlers) {
		var userId = event.getUserId();
		var progressionUpdateTask = new ProgressionUpdateTask(event, progressionHandlers);
		var updatedUserState = userStateService.updateUserStateIfPresent(userId, progressionUpdateTask);
		updatedUserState.ifPresentOrElse(rewardService::sendRewards,
				() -> log.debug("User state is not found for userId={}, skipping {}", userId, event));
	}
}
