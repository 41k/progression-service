package root.infrastructure.dto;

import java.util.List;

import lombok.Value;
import root.application.model.Configuration;

@Value
public class AllConfigurationsResponse {

	List<ConfigurationInfoDto> configurations;

	public AllConfigurationsResponse(List<Configuration> configurations) {
		this.configurations = configurations.stream().map(ConfigurationInfoDto::new).toList();
	}
}
