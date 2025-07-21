package root.application.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.Event;
import root.application.model.UserState;

@Slf4j
@RequiredArgsConstructor
public class ProgressionService {

	private final UserStateService userStateService;
	private final RewardService rewardService;

	public void process(Event event) {
		var progressionHandlers = getEligibleProgressionHandlers(event);
		if (progressionHandlers.isEmpty()) {
			log.debug("No eligible progression handlers for {}", event);
		}
		process(event, progressionHandlers);
	}

	private List<ProgressionHandler> getEligibleProgressionHandlers(Event event) {
		return List.of(); // todo
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
			updatedUserState = progressionHandler.handle(event, updatedUserState);
		}
		return updatedUserState;
	}
}
