package root.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplate;

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
}
