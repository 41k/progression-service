package root.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {

	private static final String CONTROLLER_PACKAGE = "root.infrastructure.controller";

	@Bean
	public OpenAPI apiInfo() {
		return new OpenAPI().info(new Info().title("Progression Service"));
	}

	@Bean
	public GroupedOpenApi adminApi() {
		return GroupedOpenApi.builder()
				.group("Admin API")
				.packagesToScan(CONTROLLER_PACKAGE)
				.pathsToMatch("/**/admin/**")
				.build();
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
				.group("Public API")
				.packagesToScan(CONTROLLER_PACKAGE)
				.pathsToMatch("/**/public/**")
				.build();
	}
}
