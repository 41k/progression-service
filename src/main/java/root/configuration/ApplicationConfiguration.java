package root.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

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
			@Value("${segmentation.connection-timeout-millis:10000}") long connectionTimeoutMillis,
			@Value("${segmentation.read-timeout-millis:10000}") long readTimeoutMillis
	) {
		return restTemplateBuilder
				.setConnectTimeout(Duration.ofMillis(connectionTimeoutMillis))
				.setReadTimeout(Duration.ofMillis(readTimeoutMillis))
				.build();
	}
}
