package root.application.service.progression.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import root.application.model.ProgressionType;
import root.application.model.event.Event;
import root.application.model.event.Source2Event;

@Component
public class Source2EventProgressionHandler extends ProgressionHandler {

	@Override
	public boolean isEligible(Event event) {
		return StringUtils.isNotBlank(event.getUserId());
	}

	@Override
	protected ProgressionType getProgressionType() {
		return ProgressionType.SOURCE_2;
	}

	@Override
	protected Long calculateProgressionValue(Long currentProgressionValue, Event event) {
		var source2Event = (Source2Event) event;
		return currentProgressionValue + source2Event.getAmount();
	}
}
