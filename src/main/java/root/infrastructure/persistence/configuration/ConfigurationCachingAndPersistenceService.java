package root.infrastructure.persistence.configuration;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import root.application.model.Configuration;
import root.application.service.ConfigurationService;
import root.configuration.properties.ConfigurationsCacheProperties;

@Service
@RequiredArgsConstructor
public class ConfigurationCachingAndPersistenceService implements ConfigurationService {

	private final ConfigurationsCacheProperties cacheProperties;
	private final Cache<String, Map<Long, ConfigurationEntity>> cache;
	private final CacheLoader<String, Map<Long, ConfigurationEntity>> cacheLoader;
	private final Clock clock;

	@Override
	public Configuration getCachedActiveConfigurationById(long configurationId) {
		var configurations = cache.get(cacheProperties.name(), this::loadCache);
		return Optional.ofNullable(configurations.get(configurationId))
				.map(this::toModel)
				.filter(configuration -> configuration.isActive(clock.millis()))
				.orElse(null);
	}

	@SneakyThrows
	private Map<Long, ConfigurationEntity> loadCache(String cacheName) {
		return cacheLoader.load(cacheName);
	}

	private Configuration toModel(ConfigurationEntity entity) {
		return Configuration.builder()
				.id(entity.getId())
				.startTimestamp(entity.getStartTimestamp())
				.endTimestamp(entity.getEndTimestamp())
				.updateTimestamp(entity.getUpdateTimestamp())
				.segmentedProgressionsConfiguration(entity.getSegmentedProgressionsConfiguration())
				.build();
	}
}
