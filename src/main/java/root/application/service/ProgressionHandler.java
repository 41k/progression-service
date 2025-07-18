package root.application.service;

import root.application.model.Event;
import root.application.model.UserState;

public interface ProgressionHandler {

	boolean isEligible(Event event);

	UserState handle(Event event, UserState userState);

	default String getName() {
		return this.getClass().getSimpleName();
	}
}
