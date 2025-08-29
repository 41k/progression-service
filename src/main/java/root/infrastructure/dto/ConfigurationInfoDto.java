package root.infrastructure.dto;

import root.application.model.Configuration;

public record ConfigurationInfoDto(
		long id,
		long startTimestamp,
		long endTimestamp,
		long updateTimestamp
) {
	public ConfigurationInfoDto(Configuration configuration) {
		this(
				configuration.id(),
				configuration.startTimestamp(),
				configuration.endTimestamp(),
				configuration.updateTimestamp()
		);
	}
}
