package root.application.service;

import java.util.List;

import root.application.model.Configuration;
import root.infrastructure.dto.ConfigurationRequest;

public interface ConfigurationService {

	Long createConfiguration(ConfigurationRequest request);

	List<Configuration> getConfigurations();

	Configuration getConfigurationById(Long id);

	void updateConfiguration(Long id, ConfigurationRequest request);

	void deleteConfiguration(Long id);

	Configuration getCachedActiveConfigurationById(long configurationId);
}
