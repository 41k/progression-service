package root.application.service;

import root.application.model.ProgressionsConfiguration;

public interface ConfigurationService {

	ProgressionsConfiguration getCachedEnabledActiveConfigurationById(long configurationId);
}
