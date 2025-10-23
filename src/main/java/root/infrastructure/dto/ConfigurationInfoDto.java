package root.infrastructure.dto;

import lombok.Builder;

@Builder
public record ConfigurationInfoDto(
		long id,
		long startTimestamp,
		long endTimestamp,
		long updateTimestamp
) {
}
