package root.application.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import root.application.model.UserProgressionsConfiguration;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

	private final ConfigurationPersistenceService configurationPersistenceService;
	private final Clock clock;
	@Value("${segmentation-refresh-period-millis:300000}")
	private final long segmentationRefreshPeriodInMillis;

	public Optional<UserProgressionsConfiguration> getUpdatedConfiguration(String userId,
	                                                                       UserProgressionsConfiguration currentConfiguration) {
		var configuration = configurationPersistenceService.getCachedEnabledActiveConfigurationById(currentConfiguration.id());
		if (configuration == null) {
			return Optional.empty();
		}
		if (shouldRefreshSegmentation(currentConfiguration)) {
			return Optional.empty(); // todo
		}
		return Optional.of(currentConfiguration);
	}

	private boolean shouldRefreshSegmentation(UserProgressionsConfiguration currentConfiguration) {
		return clock.millis() - currentConfiguration.updateTimestamp() > segmentationRefreshPeriodInMillis;
	}
}
