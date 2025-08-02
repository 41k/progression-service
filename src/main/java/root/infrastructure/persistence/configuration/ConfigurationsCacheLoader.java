package root.infrastructure.persistence.configuration;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.CacheLoader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConfigurationsCacheLoader implements CacheLoader<String, Map<Long, ConfigurationEntity>> {

	private final ConfigurationRepository repository;
	private final Clock clock;

	@Override
	public Map<Long, ConfigurationEntity> load(String key) {
		return repository.getActiveAndPendingConfigurations(clock.millis())
				.stream()
				.collect(Collectors.toMap(ConfigurationEntity::getId, Function.identity()));
	}
}
