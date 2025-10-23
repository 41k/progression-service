package root.infrastructure.persistence.configuration;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.CacheLoader;

import lombok.RequiredArgsConstructor;
import root.application.model.Configuration;
import root.infrastructure.ConfigurationMapper;

@Component
@RequiredArgsConstructor
public class ConfigurationsCacheLoader implements CacheLoader<String, Map<Long, Configuration>> {

	private final ConfigurationRepository repository;
	private final Clock clock;
	private final ConfigurationMapper mapper;

	@Override
	public Map<Long, Configuration> load(String key) {
		return repository.getActiveAndPendingConfigurations(clock.millis())
				.stream()
				.map(mapper::toModel)
				.collect(Collectors.toMap(Configuration::id, Function.identity()));
	}
}
