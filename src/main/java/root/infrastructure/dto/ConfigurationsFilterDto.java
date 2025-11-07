package root.infrastructure.dto;

import lombok.Builder;

@Builder
public record ConfigurationsFilterDto(
		String name,
		Long startTimestamp,
		Long endTimestamp
) {
}
