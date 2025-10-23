package root.infrastructure.dto;

import java.util.Map;

import lombok.Builder;
import root.application.model.ProgressionType;

@Builder
public record ConfigurationResponse(
		long id,
		long startTimestamp,
		long endTimestamp,
		long updateTimestamp,
		Map<String, Map<ProgressionType, ProgressionConfigurationDto>> segmentedProgressionsConfiguration
) {
}
