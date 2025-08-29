package root.infrastructure.dto;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;
import root.application.model.Configuration;
import root.application.model.ProgressionType;

@Value
public class ConfigurationResponse {

	long id;
	long startTimestamp;
	long endTimestamp;
	long updateTimestamp;
	Map<String, Map<ProgressionType, ProgressionConfigurationDto>> segmentedProgressionsConfiguration;

	public ConfigurationResponse(Configuration configuration) {
		id = configuration.id();
		startTimestamp = configuration.startTimestamp();
		endTimestamp = configuration.endTimestamp();
		updateTimestamp = configuration.updateTimestamp();
		segmentedProgressionsConfiguration = configuration.segmentedProgressionsConfiguration().entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e1 -> e1.getValue().entrySet().stream().collect(Collectors.toMap(
								Map.Entry::getKey,
								e2 -> ProgressionConfigurationDto.fromModel(e2.getValue())
						))));
	}
}
