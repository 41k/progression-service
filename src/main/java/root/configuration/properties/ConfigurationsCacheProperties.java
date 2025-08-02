package root.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties("configurations-cache")
@Validated
public record ConfigurationsCacheProperties(
		@NotBlank
		String name,
		@Min(100)
		int expirationMillis,
		@Min(100)
		int refreshMillis
) {
}
