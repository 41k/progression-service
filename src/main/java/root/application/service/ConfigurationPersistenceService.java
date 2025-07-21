package root.application.service;

import java.util.Optional;

import root.application.model.ProgressionsConfiguration;

public interface ConfigurationPersistenceService {

	ProgressionsConfiguration getCachedEnabledActiveConfigurationById(long configurationId);
}
