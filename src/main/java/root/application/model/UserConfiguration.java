package root.application.model;

import java.util.Map;

import lombok.Builder;

@Builder
public record UserConfiguration(
		long id,
		long updateTimestamp,
		Map<ProgressionType, ProgressionConfiguration> progressionsConfiguration
) {
}
