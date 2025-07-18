package root.application.model;

import java.util.Map;

import lombok.Builder;

@Builder
public record UserProgressionsConfiguration(
		long configurationId,
		long creationTimestamp,
		Map<String, ProgressionConfiguration> progressionsConfigurations
) {
}
