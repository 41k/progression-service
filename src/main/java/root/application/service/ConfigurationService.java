package root.application.service;

import java.util.List;

import root.application.model.Configuration;
import root.infrastructure.dto.ConfigurationDto;

public interface ConfigurationService {

	Long createConfiguration(ConfigurationDto configurationDto);

	List<Configuration> getConfigurations();

	Configuration getConfigurationById(Long id);

	void updateConfiguration(Long id, ConfigurationDto configurationDto);

	void deleteConfiguration(Long id);

	Configuration getCachedActiveConfigurationById(long configurationId);
}
