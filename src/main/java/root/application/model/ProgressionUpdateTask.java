package root.application.model;

import java.util.List;
import java.util.function.Function;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import root.application.model.event.Event;
import root.application.service.progression.handler.ProgressionHandler;

@Value
@Slf4j
public class ProgressionUpdateTask implements Function<UserState, UserState> {

	Event event;
	List<ProgressionHandler> progressionHandlers;

	@Override
	public UserState apply(UserState userState) {
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
