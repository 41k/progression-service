package unit.infrastructure.persistence.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.CONFIGURATION;
import static unit.UnitTestData.CONFIGURATION_ENTITY;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import root.infrastructure.ConfigurationMapperImpl;
import root.infrastructure.persistence.configuration.ConfigurationRepository;
import root.infrastructure.persistence.configuration.ConfigurationsCacheLoader;

@ExtendWith(MockitoExtension.class)
public class ConfigurationsCacheLoaderTest {
	@Mock
	private ConfigurationRepository repository;
	@Mock
	private Clock clock;
	@Spy
	private ConfigurationMapperImpl mapper;
	@InjectMocks
	private ConfigurationsCacheLoader configurationsCacheLoader;

	@Test
	void load_shouldReturnActiveAndPendingConfigurations() {
		// given
		var id1 = 1L;
		var id2 = 2L;
		var configuration1 = CONFIGURATION.toBuilder().id(id1).build();
		var configurationEntity1 = CONFIGURATION_ENTITY.toBuilder().id(id1).build();
		var configuration2 = CONFIGURATION.toBuilder().id(id2).build();
		var configurationEntity2 = CONFIGURATION_ENTITY.toBuilder().id(id2).build();
		var currentTimestamp = 100L;

		// and
		when(clock.millis()).thenReturn(currentTimestamp);
		when(repository.getActiveAndPendingConfigurations(currentTimestamp)).thenReturn(List.of(configurationEntity1, configurationEntity2));

		// when
		var configurations = configurationsCacheLoader.load("cache-name");

		// then
		assertThat(configurations).isEqualTo(Map.of(
				id1, configuration1,
				id2, configuration2
		));
	}
}
