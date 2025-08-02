package root.infrastructure.persistence.configuration;

import org.springframework.stereotype.Service;

import root.application.model.ProgressionsConfiguration;
import root.application.service.ConfigurationService;

@Service
public class ConfigurationCachingAndPersistenceService implements ConfigurationService {

	@Override
	public ProgressionsConfiguration getCachedEnabledActiveConfigurationById(long configurationId) {
		return null; // todo
	}
}
