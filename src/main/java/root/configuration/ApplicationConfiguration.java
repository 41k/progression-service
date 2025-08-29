package root.configuration;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.aerospike.repository.config.EnableAerospikeRepositories;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import root.configuration.properties.ConfigurationsCacheProperties;
import root.configuration.properties.ProgressionProperties;
import root.configuration.properties.SegmentationProperties;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.configuration.ConfigurationsCacheLoader;
import root.infrastructure.persistence.state.UserStateDocumentRepository;

@Configuration
@EnableConfigurationProperties({
		SegmentationProperties.class,
		ConfigurationsCacheProperties.class,
		ProgressionProperties.class
})
@EnableAerospikeRepositories(basePackageClasses = {UserStateDocumentRepository.class})
public class ApplicationConfiguration {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public RetryTemplate optimisticLockRetryTemplate(
			@Value("${optimistic-lock.retry.count:3}") int maxAttempts,
			@Value("${optimistic-lock.retry.interval:200}") long interval
	) {
		return RetryTemplate.builder()
				.retryOn(OptimisticLockingFailureException.class)
				.maxAttempts(maxAttempts)
				.fixedBackoff(interval)
				.build();
	}

	@Bean
	public RestTemplate segmentationRestTemplate(
			RestTemplateBuilder restTemplateBuilder,
			SegmentationProperties properties
	) {
		return restTemplateBuilder
				.setConnectTimeout(Duration.ofMillis(properties.connectionTimeoutMillis()))
				.setReadTimeout(Duration.ofMillis(properties.readTimeoutMillis()))
				.build();
	}

	@Bean
	public Cache<String, Map<Long, ConfigurationEntity>> configurationsCache(
			ConfigurationsCacheProperties cacheProperties,
			ConfigurationsCacheLoader cacheLoader
	) {
		var cache = Caffeine.newBuilder()
				.expireAfterWrite(cacheProperties.expirationMillis(), TimeUnit.MILLISECONDS)
				.refreshAfterWrite(cacheProperties.refreshMillis(), TimeUnit.MILLISECONDS)
				.build(cacheLoader);
		var cacheName = cacheProperties.name();
		cache.put(cacheName, cacheLoader.load(cacheName));
		return cache;
	}
}
