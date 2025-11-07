package root.infrastructure.dto;

import lombok.Builder;

@Builder
public record ConfigurationInfoDto(
		long id,
		String name,
		long startTimestamp,
		long endTimestamp,
		long updateTimestamp
) {
}
