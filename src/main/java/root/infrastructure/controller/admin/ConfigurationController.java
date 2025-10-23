package root.infrastructure.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.ConfigurationsResponse;
import root.infrastructure.persistence.configuration.ConfigurationPersistenceService;

@RestController
@RequestMapping(path = "/${spring.application.name}/admin/v1/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class ConfigurationController {

	private final ConfigurationPersistenceService configurationService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long createConfiguration(@RequestBody @Valid ConfigurationRequest request) {
		log.info("Create configuration: {}", request);
		return configurationService.createConfiguration(request);
	}

	// todo: pagination and filtering
	@GetMapping
	public ConfigurationsResponse getConfigurations() {
		log.info("Get all configurations");
		return configurationService.getConfigurations();
	}

	@GetMapping("/{id}")
	public ConfigurationResponse getConfiguration(@PathVariable Long id) {
		log.info("Get configuration with id={}", id);
		return configurationService.getConfigurationById(id);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateConfiguration(@PathVariable Long id, @RequestBody @Valid ConfigurationRequest request) {
		log.info("Update configuration with id={}: {}", id, request);
		configurationService.updateConfiguration(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteConfiguration(@PathVariable Long id) {
		log.info("Delete configuration with id={}", id);
		configurationService.deleteConfiguration(id);
	}
}
