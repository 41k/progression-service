package root.application.service.progression.handler;

import org.springframework.stereotype.Component;

import root.application.model.ProgressionType;
import root.application.model.event.Event;
import root.application.model.event.Source1Event;

@Component
public class Source1EventTotalProgressionHandler extends Source1EventProgressionHandler {

	@Override
	public boolean isEligible(Event event) {
		var source1Event = (Source1Event) event;
		return source1Event.getUserId() != null;
	}

	@Override
	protected ProgressionType getProgressionType() {
		return ProgressionType.SOURCE_1_TOTAL;
	}
}
