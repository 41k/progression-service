package root.application.service;

import root.application.model.ProgressionsConfiguration;

// todo: CRUD + controller
public interface ProgressionsConfigurationService {

	ProgressionsConfiguration getCachedActiveConfigurationById(long configurationId);
}
