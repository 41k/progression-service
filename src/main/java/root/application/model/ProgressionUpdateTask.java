package root.application.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import root.application.model.event.Event;
import root.application.service.progression.handler.ProgressionHandler;

@Slf4j
@Data
public class ProgressionUpdateTask implements Function<UserState, UserState> {

	private final Event event;
	private final List<ProgressionHandler> progressionHandlers;
	private final List<Reward> outcome = new ArrayList<>();

	@Override
	public UserState apply(UserState userState) {
		for (var progressionHandler : progressionHandlers) {
			try {
				progressionHandler.handle(event, userState).ifPresent(outcome::add);
			} catch (Exception e) {
				log.error("{} failed to handle {}", progressionHandler.getName(), event, e);
			}
		}
		return userState;
	}
}
