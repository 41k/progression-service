package unit.root.infrastructure.persistence.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static unit.TestData.CONFIGURATION;
import static unit.TestData.CONFIGURATION_ENTITY;

import java.time.Clock;
import java.util.Map;
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
import root.infrastructure.persistence.configuration.ConfigurationCachingAndPersistenceService;
import root.infrastructure.persistence.configuration.ConfigurationEntity;

@ExtendWith(MockitoExtension.class)
public class ConfigurationCachingAndPersistenceServiceTest {

	private static final String CACHE_NAME = "configurations-cache";
	private static final ConfigurationEntity CONFIGURATION_ENTITY_1 = CONFIGURATION_ENTITY.toBuilder()
			.id(1L)
			.startTimestamp(100L)
			.endTimestamp(300L)
			.build();
	private static final ConfigurationEntity CONFIGURATION_ENTITY_2 = CONFIGURATION_ENTITY.toBuilder()
			.id(2L)
			.startTimestamp(200L)
			.endTimestamp(500L)
			.build();
	private static final ConfigurationEntity CONFIGURATION_ENTITY_3 = CONFIGURATION_ENTITY.toBuilder()
			.id(3L)
			.startTimestamp(100L)
			.endTimestamp(400L)
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
	private Clock clock;
	@InjectMocks
	private ConfigurationCachingAndPersistenceService configurationCachingAndPersistenceService;

	@Test
	void shouldReturnCachedActiveConfigurationById() {
		// given
		var expectedConfiguration = CONFIGURATION.toBuilder()
				.id(3L)
				.startTimestamp(100L)
				.endTimestamp(400L)
				.build();

		// and
		mockCache();
		when(clock.millis()).thenReturn(250L);

		// when
		var configuration = configurationCachingAndPersistenceService.getCachedActiveConfigurationById(3L);

		// then
		assertThat(configuration).isEqualTo(expectedConfiguration);
	}

	@Test
	void shouldReturnNull_ifCachedActiveConfigurationIsNotFoundById() {
		// given
		mockCache();

		// when
		var configuration = configurationCachingAndPersistenceService.getCachedActiveConfigurationById(4L);

		// then
		assertThat(configuration).isNull();
	}

	@Test
	void shouldReturnNull_ifConfigurationIsFoundByIdButIsNotActive() {
		// given
		mockCache();
		when(clock.millis()).thenReturn(600L);

		// when
		var configuration = configurationCachingAndPersistenceService.getCachedActiveConfigurationById(2L);

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
