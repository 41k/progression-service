package root.application.service.progression.handler;

import java.util.Optional;

import root.application.model.ProgressionType;
import root.application.model.Reward;
import root.application.model.UserState;
import root.application.model.event.Event;

public abstract class ProgressionHandler {

	private static final Long INITIAL_PROGRESSION_VALUE = 0L;

	public Optional<Reward> handle(Event event, UserState userState) {
		var progressionType = getProgressionType();
		var progressionConfiguration = userState.getProgressionConfiguration(progressionType);
		if (progressionConfiguration == null) {
			return Optional.empty();
		}
		var currentProgressionValue = userState.getProgressions().getOrDefault(progressionType, INITIAL_PROGRESSION_VALUE);
		var adjustedProgressionValue = getAdjustedProgressionValue(currentProgressionValue, event);
		if (adjustedProgressionValue < progressionConfiguration.progressionTarget()) {
			userState.getProgressions().put(progressionType, adjustedProgressionValue);
			return Optional.empty();
		}
		userState.getProgressions().put(progressionType, INITIAL_PROGRESSION_VALUE);
		return Optional.of(progressionConfiguration.reward());
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public abstract boolean isEligible(Event event);

	protected abstract ProgressionType getProgressionType();

	protected abstract Long getAdjustedProgressionValue(Long currentProgressionValue, Event event);
}
