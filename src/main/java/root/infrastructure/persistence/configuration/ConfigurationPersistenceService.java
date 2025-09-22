package root.infrastructure.persistence.configuration;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import root.application.model.Configuration;
import root.application.service.ConfigurationService;
import root.configuration.properties.ConfigurationsCacheProperties;
import root.infrastructure.dto.ConfigurationRequest;

@Service
@RequiredArgsConstructor
public class ConfigurationPersistenceService implements ConfigurationService {

	private final ConfigurationsCacheProperties cacheProperties;
	private final Cache<String, Map<Long, ConfigurationEntity>> cache;
	private final CacheLoader<String, Map<Long, ConfigurationEntity>> cacheLoader;
	private final ConfigurationRepository repository;
	private final Clock clock;

	// todo: validate that time range does not intersect with other configurations
	public Long createConfiguration(ConfigurationRequest request) {
		var configuration = toEntity(request);
		return repository.save(configuration).getId();
	}

	public List<Configuration> getConfigurations() {
		return repository.findAll().stream().map(ConfigurationEntity::toModel).toList();
	}

	public Configuration getConfigurationById(Long id) {
		return findById(id).toModel();
	}

	// todo: validate that time range does not intersect with other configurations
	@Transactional
	public void updateConfiguration(Long id, ConfigurationRequest request) {
		var existingConfiguration = findById(id);
		var updatedConfiguration = toEntity(request).toBuilder().id(existingConfiguration.getId()).build();
		repository.save(updatedConfiguration);
	}

	public void deleteConfiguration(Long id) {
		repository.deleteById(id);
	}

	@Override
	public Configuration getActiveConfiguration() {
		return cache.get(cacheProperties.name(), this::loadCache).values()
				.stream()
				.map(ConfigurationEntity::toModel)
				.filter(configuration -> configuration.isActive(clock.millis()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public Configuration getActiveConfigurationById(long configurationId) {
		var configurations = cache.get(cacheProperties.name(), this::loadCache);
		return Optional.ofNullable(configurations.get(configurationId))
				.map(ConfigurationEntity::toModel)
				.filter(configuration -> configuration.isActive(clock.millis()))
				.orElse(null);
	}

	@SneakyThrows
	private Map<Long, ConfigurationEntity> loadCache(String cacheName) {
		return cacheLoader.load(cacheName);
	}

	private ConfigurationEntity toEntity(ConfigurationRequest request) {
		var segmentedProgressionsConfiguration = request.segmentedProgressionsConfiguration().entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e1 -> e1.getValue().entrySet().stream().collect(Collectors.toMap(
								Map.Entry::getKey,
								e2 -> e2.getValue().toModel()
						))));
		return ConfigurationEntity.builder()
				.startTimestamp(request.startTimestamp())
				.endTimestamp(request.endTimestamp())
				.updateTimestamp(clock.millis())
				.segmentedProgressionsConfiguration(segmentedProgressionsConfiguration)
				.build();
	}

	private ConfigurationEntity findById(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Configuration is not found by id=" + id));
	}
}
