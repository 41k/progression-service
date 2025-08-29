package functional;

import static functional.FunctionalTestData.CURRENT_TIMESTAMP;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FunctionalTestConfiguration {

	@Bean
	public Clock clock() {
		return Clock.fixed(Instant.ofEpochMilli(CURRENT_TIMESTAMP), ZoneId.systemDefault());
	}
}
