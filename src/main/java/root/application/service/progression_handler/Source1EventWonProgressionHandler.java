package root.application.service.progression_handler;

import org.springframework.stereotype.Component;

import root.application.model.ProgressionType;
import root.application.model.event.Event;
import root.application.model.event.Source1Event;

@Component
public class Source1EventWonProgressionHandler extends Source1EventProgressionHandler {

	private static final String WON_RESULT = "WON";

	@Override
	public boolean isEligible(Event event) {
		var source1Event = (Source1Event) event;
		return source1Event.getUserId() != null && WON_RESULT.equalsIgnoreCase(source1Event.getResult());
	}

	@Override
	protected ProgressionType getProgressionType() {
		return ProgressionType.SOURCE_1_WON;
	}
}
