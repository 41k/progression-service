package root.infrastructure.dto;

import java.util.List;

public record ConfigurationsResponse(
		List<ConfigurationInfoDto> configurations
) {
}
