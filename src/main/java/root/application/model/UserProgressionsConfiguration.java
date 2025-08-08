package root.application.model;

import java.util.Map;

import lombok.Builder;

@Builder
public record UserProgressionsConfiguration(
		long id,
		long updateTimestamp,
		Map<ProgressionType, ProgressionConfiguration> progressionsConfiguration
) {
}
