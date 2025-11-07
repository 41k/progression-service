package root.infrastructure.persistence.configuration;

import java.time.Clock;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
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
import root.infrastructure.dto.ConfigurationInfoDto;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.ConfigurationsFilterDto;
import root.infrastructure.dto.PaginatedResponse;

@Service
@RequiredArgsConstructor
public class ConfigurationPersistenceService implements ConfigurationService {

	private final ConfigurationsCacheProperties cacheProperties;
	private final Cache<String, Map<Long, Configuration>> cache;
	private final CacheLoader<String, Map<Long, Configuration>> cacheLoader;
	private final SpecificationBuilder specificationBuilder;
	private final ConfigurationRepository repository;
	private final ConfigurationMapper mapper;
	private final Clock clock;

	@Transactional
	public Long createConfiguration(ConfigurationRequest request) {
		validateTimeRange(request);
		var configuration = mapper.toEntity(request)
				.toBuilder()
				.updateTimestamp(clock.millis())
				.build();
		return repository.save(configuration).getId();
	}

	public PaginatedResponse<ConfigurationInfoDto> getConfigurations(ConfigurationsFilterDto filter, Pageable pageSettings) {
		var specification = specificationBuilder.build(filter);
		var page = repository.findAll(specification, pageSettings);
		return mapper.toResponse(page);
	}

	public ConfigurationResponse getConfigurationById(Long id) {
		return mapper.toResponse(findById(id));
	}

	@Transactional
	public void updateConfiguration(Long id, ConfigurationRequest request) {
		validateTimeRange(id, request);
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

	private void validateTimeRange(ConfigurationRequest request) {
		validateTimeRange(null, request);
	}

	private void validateTimeRange(Long id, ConfigurationRequest request) {
		var hasIntersectionWithOtherConfigurations =
				repository.getConfigurationsWithTimeRangeIntersection(request.startTimestamp(), request.endTimestamp())
						.stream()
						.anyMatch(configuration -> !configuration.getId().equals(id));
		if (hasIntersectionWithOtherConfigurations) {
			throw new IllegalArgumentException("Configuration time range intersects with another existing configuration");
		}
	}
}
