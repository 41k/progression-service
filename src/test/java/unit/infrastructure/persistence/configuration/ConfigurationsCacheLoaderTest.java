package unit.infrastructure.persistence.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.CONFIGURATION_ENTITY;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import root.infrastructure.persistence.configuration.ConfigurationRepository;
import root.infrastructure.persistence.configuration.ConfigurationsCacheLoader;

@ExtendWith(MockitoExtension.class)
public class ConfigurationsCacheLoaderTest {
	@Mock
	private ConfigurationRepository repository;
	@Mock
	private Clock clock;
	@InjectMocks
	private ConfigurationsCacheLoader configurationsCacheLoader;

	@Test
	void load_shouldReturnActiveAndPendingConfigurations() {
		// given
		var configuration1 = CONFIGURATION_ENTITY.toBuilder().id(1L).build();
		var configuration2 = CONFIGURATION_ENTITY.toBuilder().id(2L).build();
		var currentTimestamp = 100L;

		// and
		when(clock.millis()).thenReturn(currentTimestamp);
		when(repository.getActiveAndPendingConfigurations(currentTimestamp)).thenReturn(List.of(configuration1, configuration2));

		// when
		var configurations = configurationsCacheLoader.load("cache-name");

		// then
		assertThat(configurations).isEqualTo(Map.of(
				configuration1.getId(), configuration1,
				configuration2.getId(), configuration2
		));
	}
}
