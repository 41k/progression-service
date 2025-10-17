package unit.infrastructure.persistence.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.CONFIGURATION;
import static unit.UnitTestData.CONFIGURATION_ENTITY;
import static unit.UnitTestData.CONFIGURATION_ID;
import static unit.UnitTestData.CONFIGURATION_REQUEST;
import static unit.UnitTestData.CONFIGURATION_UPDATE_TIMESTAMP;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;

import lombok.SneakyThrows;
import root.configuration.properties.ConfigurationsCacheProperties;
import root.infrastructure.persistence.configuration.ConfigurationPersistenceService;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.configuration.ConfigurationRepository;

@ExtendWith(MockitoExtension.class)
public class ConfigurationPersistenceServiceTest {

	private static final String CACHE_NAME = "configurations-cache";
	private static final ConfigurationEntity CONFIGURATION_ENTITY_1 = CONFIGURATION_ENTITY.toBuilder()
			.id(1L)
			.startTimestamp(100L)
			.endTimestamp(200L)
			.build();
	private static final ConfigurationEntity CONFIGURATION_ENTITY_2 = CONFIGURATION_ENTITY.toBuilder()
			.id(2L)
			.startTimestamp(300L)
			.endTimestamp(400L)
			.build();
	private static final ConfigurationEntity CONFIGURATION_ENTITY_3 = CONFIGURATION_ENTITY.toBuilder()
			.id(3L)
			.startTimestamp(500L)
			.endTimestamp(600L)
			.build();
	private static final Map<Long, ConfigurationEntity> CACHED_CONFIGURATIONS = Map.of(
			CONFIGURATION_ENTITY_1.getId(), CONFIGURATION_ENTITY_1,
			CONFIGURATION_ENTITY_2.getId(), CONFIGURATION_ENTITY_2,
			CONFIGURATION_ENTITY_3.getId(), CONFIGURATION_ENTITY_3
	);

	@Mock
	private ConfigurationsCacheProperties cacheProperties;
	@Mock
	private Cache<String, Map<Long, ConfigurationEntity>> cache;
	@Mock
	private CacheLoader<String, Map<Long, ConfigurationEntity>> cacheLoader;
	@Mock
	private ConfigurationRepository repository;
	@Mock
	private Clock clock;
	@InjectMocks
	private ConfigurationPersistenceService configurationPersistenceService;

	@Test
	void createConfiguration() {
		// given
		var configurationEntityToSave = CONFIGURATION_ENTITY.toBuilder().id(null).build();

		// and
		when(clock.millis()).thenReturn(CONFIGURATION_UPDATE_TIMESTAMP);
		when(repository.save(configurationEntityToSave)).thenReturn(CONFIGURATION_ENTITY);

		// when
		var id = configurationPersistenceService.createConfiguration(CONFIGURATION_REQUEST);

		// then
		assertThat(id).isEqualTo(CONFIGURATION_ID);
	}

	@Test
	void getConfigurationById() {
		when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(CONFIGURATION_ENTITY));

		var configuration = configurationPersistenceService.getConfigurationById(CONFIGURATION_ID);

		assertThat(configuration).isEqualTo(CONFIGURATION);
	}

	@Test
	void getConfigurationById_shouldThrowException_ifConfigurationIsNotFoundById() {
		when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> configurationPersistenceService.getConfigurationById(CONFIGURATION_ID))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Configuration is not found by id=" + CONFIGURATION_ID);
	}

	@Test
	void getConfigurations() {
		when(repository.findAll()).thenReturn(List.of(CONFIGURATION_ENTITY, CONFIGURATION_ENTITY));

		var configurations = configurationPersistenceService.getConfigurations();

		assertThat(configurations).isEqualTo(List.of(CONFIGURATION, CONFIGURATION));
	}

	@Test
	void updateConfiguration() {
		// given
		var existingConfiguration = ConfigurationEntity.builder().id(CONFIGURATION_ID).updateTimestamp(10L).build();

		// and
		when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(existingConfiguration));
		when(clock.millis()).thenReturn(CONFIGURATION_UPDATE_TIMESTAMP);

		// when
		configurationPersistenceService.updateConfiguration(CONFIGURATION_ID, CONFIGURATION_REQUEST);

		// then
		verify(repository).save(CONFIGURATION_ENTITY);
	}

	@Test
	void updateConfiguration_shouldNotUpdateConfiguration_andThrowException_ifConfigurationIsNotFoundById() {
		when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> configurationPersistenceService.updateConfiguration(CONFIGURATION_ID, CONFIGURATION_REQUEST))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Configuration is not found by id=" + CONFIGURATION_ID);
	}

	@Test
	void deleteConfiguration() {
		configurationPersistenceService.deleteConfiguration(CONFIGURATION_ID);

		verify(repository).deleteById(CONFIGURATION_ID);
	}

	@Test
	void getActiveConfigurationById_shouldReturnCachedActiveConfigurationById() {
		// given
		var expectedConfiguration = CONFIGURATION.toBuilder()
				.id(3L)
				.startTimestamp(500L)
				.endTimestamp(600L)
				.build();

		// and
		mockCache();
		when(clock.millis()).thenReturn(550L);

		// when
		var configuration = configurationPersistenceService.getActiveConfigurationById(3L);

		// then
		assertThat(configuration).isEqualTo(expectedConfiguration);
	}

	@Test
	void getActiveConfigurationById_shouldReturnNull_ifCachedActiveConfigurationIsNotFoundById() {
		// given
		mockCache();

		// when
		var configuration = configurationPersistenceService.getActiveConfigurationById(4L);

		// then
		assertThat(configuration).isNull();
	}

	@Test
	void getActiveConfigurationById_shouldReturnNull_ifConfigurationIsFoundByIdButIsNotActive() {
		// given
		mockCache();
		when(clock.millis()).thenReturn(700L);

		// when
		var configuration = configurationPersistenceService.getActiveConfigurationById(2L);

		// then
		assertThat(configuration).isNull();
	}

	@Test
	void getActiveConfiguration_shouldReturnCachedActiveConfiguration() {
		// given
		var expectedConfiguration = CONFIGURATION.toBuilder()
				.id(2L)
				.startTimestamp(300L)
				.endTimestamp(400L)
				.build();

		// and
		mockCache();
		when(clock.millis()).thenReturn(350L);

		// when
		var configuration = configurationPersistenceService.getActiveConfiguration();

		// then
		assertThat(configuration).isEqualTo(expectedConfiguration);
	}

	@Test
	void getActiveConfiguration_shouldReturnNull_ifThereIsNoActiveConfiguration() {
		// given
		mockCache();
		when(clock.millis()).thenReturn(700L);

		// when
		var configuration = configurationPersistenceService.getActiveConfiguration();

		// then
		assertThat(configuration).isNull();
	}

	@SneakyThrows
	private void mockCache() {
		when(cacheProperties.name()).thenReturn(CACHE_NAME);
		when(cacheLoader.load(CACHE_NAME)).thenReturn(CACHED_CONFIGURATIONS);
		when(cache.get(eq(CACHE_NAME), any()))
				.then(invocation -> {
					Function<String, Map<Long, ConfigurationEntity>> cacheLoaderFunction = invocation.getArgument(1);
					return cacheLoaderFunction.apply(CACHE_NAME);
				});
	}
}
