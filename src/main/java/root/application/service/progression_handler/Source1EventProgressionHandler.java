package root.application.service.progression_handler;

import root.application.model.ProgressionType;
import root.application.model.UserState;
import root.application.model.event.Event;

public abstract class Source1EventProgressionHandler implements ProgressionHandler {

	@Override
	public UserState handle(Event event, UserState userState) {
		var progressionType = getProgressionType();
		var progressionConfiguration = userState.getProgressionConfiguration(progressionType);
		if (progressionConfiguration == null) {
			return userState;
		}
		var currentProgressionValue = userState.getProgressions().getOrDefault(progressionType, INITIAL_PROGRESSION_VALUE);
		var adjustedProgressionValue = currentProgressionValue + 1;
		if (adjustedProgressionValue < progressionConfiguration.progressionTarget()) {
			userState.getProgressions().put(progressionType, adjustedProgressionValue);
			return userState;
		}
		userState.addReward(progressionConfiguration.reward());
		userState.getProgressions().put(progressionType, INITIAL_PROGRESSION_VALUE);
		return userState;
	}

	protected abstract ProgressionType getProgressionType();
}
