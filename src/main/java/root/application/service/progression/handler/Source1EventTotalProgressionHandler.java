package root.application.service.progression.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import root.application.model.ProgressionType;
import root.application.model.event.Event;

@Component
public class Source1EventTotalProgressionHandler extends ProgressionHandler {

	@Override
	public boolean isEligible(Event event) {
		return StringUtils.isNotBlank(event.getUserId());
	}

	@Override
	protected ProgressionType getProgressionType() {
		return ProgressionType.SOURCE_1_TOTAL;
	}

	@Override
	protected Long calculateProgressionValue(Long currentProgressionValue, Event event) {
		return currentProgressionValue + 1;
	}
}
