package root.application.service;

import root.application.model.Configuration;

// todo: CRUD + controller
public interface ConfigurationService {

	Configuration getCachedActiveConfigurationById(long configurationId);
}
