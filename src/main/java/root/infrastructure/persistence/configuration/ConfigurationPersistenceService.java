package root.infrastructure.persistence.configuration;

import java.time.Clock;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import root.application.model.Configuration;
import root.application.service.ConfigurationService;
import root.configuration.properties.ConfigurationsCacheProperties;
import root.infrastructure.ConfigurationMapper;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.ConfigurationsResponse;

@Service
@RequiredArgsConstructor
public class ConfigurationPersistenceService implements ConfigurationService {

	private final ConfigurationsCacheProperties cacheProperties;
	private final Cache<String, Map<Long, Configuration>> cache;
	private final CacheLoader<String, Map<Long, Configuration>> cacheLoader;
	private final ConfigurationRepository repository;
	private final ConfigurationMapper mapper;
	private final Clock clock;

	// todo: validate that time range does not intersect with other configurations
	public Long createConfiguration(ConfigurationRequest request) {
		var configuration = mapper.toEntity(request)
				.toBuilder()
				.updateTimestamp(clock.millis())
				.build();
		return repository.save(configuration).getId();
	}

	public ConfigurationsResponse getConfigurations() {
		var configurations = repository.findAll().stream().map(mapper::toDto).toList();
		return new ConfigurationsResponse(configurations);
	}

	public ConfigurationResponse getConfigurationById(Long id) {
		return mapper.toResponse(findById(id));
	}

	// todo: validate that time range does not intersect with other configurations
	@Transactional
	public void updateConfiguration(Long id, ConfigurationRequest request) {
		var existingConfiguration = findById(id);
		var updatedConfiguration = mapper.toEntity(request)
				.toBuilder()
				.id(existingConfiguration.getId())
				.updateTimestamp(clock.millis())
				.build();
		repository.save(updatedConfiguration);
	}

	public void deleteConfiguration(Long id) {
		repository.deleteById(id);
	}

	@Override
	public Configuration getActiveConfiguration() {
		return cache.get(cacheProperties.name(), this::loadCache).values()
				.stream()
				.filter(configuration -> configuration.isActive(clock.millis()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public Configuration getActiveConfigurationById(long configurationId) {
		var configurations = cache.get(cacheProperties.name(), this::loadCache);
		return Optional.ofNullable(configurations.get(configurationId))
				.filter(configuration -> configuration.isActive(clock.millis()))
				.orElse(null);
	}

	@SneakyThrows
	private Map<Long, Configuration> loadCache(String cacheName) {
		return cacheLoader.load(cacheName);
	}

	private ConfigurationEntity findById(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Configuration is not found by id=" + id));
	}
}
