package unit.infrastructure.persistence.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.CONFIGURATION;
import static unit.UnitTestData.CONFIGURATION_END_TIMESTAMP;
import static unit.UnitTestData.CONFIGURATION_ENTITY;
import static unit.UnitTestData.CONFIGURATION_ID;
import static unit.UnitTestData.CONFIGURATION_REQUEST;
import static unit.UnitTestData.CONFIGURATION_START_TIMESTAMP;
import static unit.UnitTestData.CONFIGURATION_UPDATE_TIMESTAMP;
import static unit.UnitTestData.SEGMENTED_PROGRESSIONS_CONFIGURATION_DTO;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;

import lombok.SneakyThrows;
import root.application.model.Configuration;
import root.configuration.properties.ConfigurationsCacheProperties;
import root.infrastructure.ConfigurationMapperImpl;
import root.infrastructure.dto.ConfigurationInfoDto;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.ConfigurationsResponse;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.configuration.ConfigurationPersistenceService;
import root.infrastructure.persistence.configuration.ConfigurationRepository;

@ExtendWith(MockitoExtension.class)
public class ConfigurationPersistenceServiceTest {

	private static final String CACHE_NAME = "configurations-cache";
	private static final Configuration CONFIGURATION_1 = CONFIGURATION.toBuilder()
			.id(1L)
			.startTimestamp(100L)
			.endTimestamp(200L)
			.build();
	private static final Configuration CONFIGURATION_2 = CONFIGURATION.toBuilder()
			.id(2L)
			.startTimestamp(300L)
			.endTimestamp(400L)
			.build();
	private static final Configuration CONFIGURATION_3 = CONFIGURATION.toBuilder()
			.id(3L)
			.startTimestamp(500L)
			.endTimestamp(600L)
			.build();
	private static final Map<Long, Configuration> CACHED_CONFIGURATIONS = Map.of(
			CONFIGURATION_1.id(), CONFIGURATION_1,
			CONFIGURATION_2.id(), CONFIGURATION_2,
			CONFIGURATION_3.id(), CONFIGURATION_3
	);

	@Mock
	private ConfigurationsCacheProperties cacheProperties;
	@Mock
	private Cache<String, Map<Long, Configuration>> cache;
	@Mock
	private CacheLoader<String, Map<Long, Configuration>> cacheLoader;
	@Mock
	private ConfigurationRepository repository;
	@Spy
	private ConfigurationMapperImpl mapper;
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
		// given
		var expectedConfigurationResponse = ConfigurationResponse.builder()
				.id(CONFIGURATION_ID)
				.startTimestamp(CONFIGURATION_START_TIMESTAMP)
				.endTimestamp(CONFIGURATION_END_TIMESTAMP)
				.updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP)
				.segmentedProgressionsConfiguration(SEGMENTED_PROGRESSIONS_CONFIGURATION_DTO)
				.build();

		// and
		when(repository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(CONFIGURATION_ENTITY));

		// when
		var configurationResponse = configurationPersistenceService.getConfigurationById(CONFIGURATION_ID);

		// then
		assertThat(configurationResponse).isEqualTo(expectedConfigurationResponse);
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
		// given
		var configurationEntities = List.of(
				CONFIGURATION_ENTITY.toBuilder().id(1L).startTimestamp(100L).endTimestamp(200L).build(),
				CONFIGURATION_ENTITY.toBuilder().id(2L).startTimestamp(300L).endTimestamp(400L).build()
		);
		var expectedConfigurationsResponse = new ConfigurationsResponse(List.of(
				ConfigurationInfoDto.builder().id(1L).startTimestamp(100L).endTimestamp(200L).updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP).build(),
				ConfigurationInfoDto.builder().id(2L).startTimestamp(300L).endTimestamp(400L).updateTimestamp(CONFIGURATION_UPDATE_TIMESTAMP).build()
		));

		// and
		when(repository.findAll()).thenReturn(configurationEntities);

		// when
		var configurationsResponse = configurationPersistenceService.getConfigurations();

		// then
		assertThat(configurationsResponse).isEqualTo(expectedConfigurationsResponse);
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
					Function<String, Map<Long, Configuration>> cacheLoaderFunction = invocation.getArgument(1);
					return cacheLoaderFunction.apply(CACHE_NAME);
				});
	}
}
