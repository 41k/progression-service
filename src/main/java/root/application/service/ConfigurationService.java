package root.application.service;

import root.application.model.Configuration;
import root.infrastructure.dto.ConfigurationDto;

public interface ConfigurationService {

	Long createConfiguration(ConfigurationDto configurationDto);

	Configuration getConfigurationById(Long id);

	void updateConfiguration(Long id, ConfigurationDto configurationDto);

	void deleteConfiguration(Long id);

	Configuration getCachedActiveConfigurationById(long configurationId);
}
