package root.application.service;

import root.application.model.Configuration;

public interface ConfigurationService {

	Configuration getActiveConfiguration();

	Configuration getActiveConfigurationById(long configurationId);
}
