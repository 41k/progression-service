package root.infrastructure.controller.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.infrastructure.dto.ConfigurationInfoDto;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.ConfigurationsFilterDto;
import root.infrastructure.dto.PaginatedResponse;
import root.infrastructure.dto.validation.RegexPatterns;
import root.infrastructure.persistence.configuration.ConfigurationPersistenceService;

@Tag(name = "Configuration API")
@RestController
@RequestMapping(path = "/${spring.application.name}/api/admin/v1/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
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

	@GetMapping
	public PaginatedResponse<ConfigurationInfoDto> getConfigurations(@RequestParam @Min(0) Integer pageNumber,
	                                                                 @RequestParam @Min(1) Integer pageSize,
	                                                                 @RequestParam @NotBlank String orderBy,
	                                                                 @RequestParam @Pattern(regexp = RegexPatterns.ORDER) String order,
	                                                                 @ModelAttribute @ParameterObject ConfigurationsFilterDto filter) {
		log.info("Get configurations filtered by: {}", filter);
		var sort = Sort.by(Sort.Order.by(orderBy).with(Sort.Direction.fromString(order)));
		var pageSettings = PageRequest.of(pageNumber, pageSize, sort);
		return configurationService.getConfigurations(filter, pageSettings);
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
