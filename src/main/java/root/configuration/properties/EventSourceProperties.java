package root.configuration.properties;

import java.util.Set;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Validated
public record EventSourceProperties(
		@NotNull
		Boolean enabled,
		@NotBlank
		String topic,
		@NotEmpty
		Set<String> progressionHandlers
) {
}
