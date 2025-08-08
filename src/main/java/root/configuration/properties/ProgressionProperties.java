package root.configuration.properties;

import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import root.application.model.event.EventSource;

@ConfigurationProperties("progression")
@Validated
public record ProgressionProperties(
		@NotEmpty
		Map<EventSource, EventSourceProperties> sources
) {
	public EventSourceProperties getEventSourceProperties(EventSource source) {
		return Optional.ofNullable(sources.get(source))
				.orElseThrow(() -> new RuntimeException("Event source [%s] is not configured".formatted(source.name())));
	}
}
