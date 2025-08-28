package root.infrastructure.controller.admin;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.Configuration;
import root.application.service.ConfigurationService;
import root.infrastructure.dto.ConfigurationDto;

// todo: functional tests
@RestController
@RequestMapping(path = "/${spring.application.name}/admin/v1/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class ConfigurationController {

	private final ConfigurationService configurationService;

	@PostMapping
	public Long createConfiguration(@RequestBody @Valid ConfigurationDto configuration) {
		log.info("Create configuration: {}", configuration);
		return configurationService.createConfiguration(configuration);
	}

	@GetMapping("/{id}")
	public Configuration getConfiguration(@PathVariable Long id) {
		log.info("Get configuration with id={}", id);
		return configurationService.getConfigurationById(id);
	}

	@PutMapping("/{id}")
	public void updateConfiguration(@PathVariable Long id, @RequestBody @Valid ConfigurationDto configuration) {
		log.info("Update configuration with id={}: {}", id, configuration);
		configurationService.updateConfiguration(id, configuration);
	}

	@DeleteMapping("/{id}")
	public void deleteConfiguration(@PathVariable Long id) {
		log.info("Delete configuration with id={}", id);
		configurationService.deleteConfiguration(id);
	}
}
