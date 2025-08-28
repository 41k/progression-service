package root.infrastructure.persistence.configuration;

import java.time.Clock;
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
import root.infrastructure.dto.ConfigurationDto;

@Service
@RequiredArgsConstructor
public class ConfigurationCachingAndPersistenceService implements ConfigurationService {

	private final ConfigurationsCacheProperties cacheProperties;
	private final Cache<String, Map<Long, ConfigurationEntity>> cache;
	private final CacheLoader<String, Map<Long, ConfigurationEntity>> cacheLoader;
	private final ConfigurationRepository repository;
	private final Clock clock;

	@Override
	public Long createConfiguration(ConfigurationDto configurationDto) {
		var configuration = toEntity(configurationDto);
		return repository.save(configuration).getId();
	}

	@Override
	public Configuration getConfigurationById(Long id) {
		return findById(id).toModel();
	}

	@Override
	@Transactional
	public void updateConfiguration(Long id, ConfigurationDto configurationDto) {
		var existingConfiguration = findById(id);
		var updatedConfiguration = toEntity(configurationDto).toBuilder().id(existingConfiguration.getId()).build();
		repository.save(updatedConfiguration);
	}

	@Override
	public void deleteConfiguration(Long id) {
		repository.deleteById(id);
	}

	@Override
	public Configuration getCachedActiveConfigurationById(long configurationId) {
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

	private ConfigurationEntity toEntity(ConfigurationDto dto) {
		var segmentedProgressionsConfiguration = dto.segmentedProgressionsConfiguration().entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e1 -> e1.getValue().entrySet().stream().collect(Collectors.toMap(
								Map.Entry::getKey,
								e2 -> e2.getValue().toModel()
						))));
		return ConfigurationEntity.builder()
				.startTimestamp(dto.startTimestamp())
				.endTimestamp(dto.endTimestamp())
				.updateTimestamp(clock.millis())
				.segmentedProgressionsConfiguration(segmentedProgressionsConfiguration)
				.build();
	}

	private ConfigurationEntity findById(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Configuration is not found by id=" + id));
	}
}
