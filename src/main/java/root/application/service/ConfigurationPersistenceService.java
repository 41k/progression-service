package root.application.service;

import root.application.model.ProgressionsConfiguration;

public interface ConfigurationPersistenceService {

	ProgressionsConfiguration getCachedEnabledActiveConfigurationById(long configurationId);
}
