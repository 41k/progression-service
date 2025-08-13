package root.application.service.progression.handler;

import root.application.model.event.Event;
import root.application.model.UserState;

public interface ProgressionHandler {

	Long INITIAL_PROGRESSION_VALUE = 0L;

	boolean isEligible(Event event);

	UserState handle(Event event, UserState userState);

	default String getName() {
		return this.getClass().getSimpleName();
	}
}
