package root.application.model;

import java.util.Map;
import java.util.Set;

public record ProgressionsConfiguration(
		long id,
		long updateTimestamp,
		Map<String, Map<String, ProgressionConfiguration>> segmentedProgressionsConfiguration
) {
	public Set<String> getAllSegments() {
		return segmentedProgressionsConfiguration.keySet();
	}

	public Map<String, ProgressionConfiguration> getUserProgressionsConfiguration(String segment) {
		return segmentedProgressionsConfiguration.get(segment);
	}
}
