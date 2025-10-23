package root.infrastructure.dto;

import java.util.Map;

import root.application.model.ProgressionType;

public record ConfigurationResponse(
		long id,
		long startTimestamp,
		long endTimestamp,
		long updateTimestamp,
		Map<String, Map<ProgressionType, ProgressionConfigurationDto>> segmentedProgressionsConfiguration
) {
}
