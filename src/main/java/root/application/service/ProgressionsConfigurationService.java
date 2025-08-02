package root.application.service;

import root.application.model.ProgressionsConfiguration;

public interface ProgressionsConfigurationService {

	ProgressionsConfiguration getCachedActiveConfigurationById(long configurationId);
}
