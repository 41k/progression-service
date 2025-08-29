package functional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import root.infrastructure.persistence.configuration.ConfigurationEntity;

@UtilityClass
public class FunctionalTestData {

	public static final Long CURRENT_TIMESTAMP = 600L;

	public static final String CONFIGURATION_REQUEST_BODY = getFileContent("http/request/configuration-request-body.json");
	public static final String INVALID_CONFIGURATION_REQUEST_BODY = getFileContent("http/request/invalid-configuration-request-body.json");
	public static final String CONFIGURATION_RESPONSE_BODY = getFileContent("http/response/configuration-response-body.json");
	public static final String ALL_CONFIGURATIONS_RESPONSE_BODY = getFileContent("http/response/all-configurations-response-body.json");
	private static final String CONFIGURATION_ENTITY = getFileContent("db/configuration/configuration-entity.json");
	private static final String CONFIGURATION_ENTITY_BEFORE_UPDATE = getFileContent("db/configuration/configuration-entity-before-update.json");

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@SneakyThrows
	public static ConfigurationEntity configurationEntity(Long configurationId) {
		return configurationEntity().toBuilder().id(configurationId).build();
	}

	@SneakyThrows
	public static ConfigurationEntity configurationEntity() {
		return OBJECT_MAPPER.readValue(CONFIGURATION_ENTITY, ConfigurationEntity.class);
	}

	@SneakyThrows
	public static ConfigurationEntity configurationEntityBeforeUpdate() {
		return OBJECT_MAPPER.readValue(CONFIGURATION_ENTITY_BEFORE_UPDATE, ConfigurationEntity.class);
	}

	@SneakyThrows
	private static String getFileContent(String filePath) {
		var path = Paths.get(FunctionalTestData.class.getClassLoader().getResource(filePath).toURI());
		try (var lines = Files.lines(path)) {
			return lines.collect(Collectors.joining("\n"));
		} catch (Exception e) {
			throw new IOException("Failed to load file: %s".formatted(filePath), e);
		}
	}
}
