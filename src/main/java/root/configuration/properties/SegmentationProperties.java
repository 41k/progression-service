package root.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties("segmentation")
@Validated
public record SegmentationProperties(
		@NotBlank
		String url,
		@Min(100)
		int connectionTimeoutMillis,
		@Min(100)
		int readTimeoutMillis,
		@Min(100)
		int reevaluationPeriodMillis
) {
}
