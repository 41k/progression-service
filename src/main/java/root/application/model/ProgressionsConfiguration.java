package root.application.model;

import java.util.Map;
import java.util.Set;

import lombok.Builder;

@Builder
public record ProgressionsConfiguration(
		long id,
		long startTimestamp,
		long endTimestamp,
		long updateTimestamp,
		Map<String, Map<ProgressionType, ProgressionConfiguration>> segmentedProgressionsConfiguration
) {
	public Set<String> getAllSegments() {
		return segmentedProgressionsConfiguration.keySet();
	}

	public Map<ProgressionType, ProgressionConfiguration> getUserProgressionsConfiguration(String segment) {
		return segmentedProgressionsConfiguration.get(segment);
	}

	public boolean isActive(long currentTimestamp) {
		return startTimestamp <= currentTimestamp && currentTimestamp <= endTimestamp;
	}
}
