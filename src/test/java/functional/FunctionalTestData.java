package functional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.state.UserStateDocument;

@UtilityClass
public class FunctionalTestData {

	public static final String USER_ID = "1000";
	public static final String SEGMENT_1 = "segment-1";
	public static final String SEGMENT_2 = "segment-2";
	public static final Long CURRENT_TIMESTAMP = 600L;

	public static final String SEGMENTATION_URL = "/segmentation-service/api/v1/evaluate";

	public static final String CONFIGURATION_REQUEST_BODY = getFileContent("http/request/configuration-request-body.json");
	public static final String INVALID_CONFIGURATION_REQUEST_BODY = getFileContent("http/request/invalid-configuration-request-body.json");
	public static final String CONFIGURATION_RESPONSE_BODY = getFileContent("http/response/configuration-response-body.json");
	public static final String ALL_CONFIGURATIONS_RESPONSE_BODY = getFileContent("http/response/all-configurations-response-body.json");
	public static final String USER_STATE_RESPONSE_BODY = getFileContent("http/response/user-state-response-body.json");
	public static final String SEGMENTATION_REQUEST_BODY = getFileContent("http/request/segmentation-request-body.json");
	public static final String SEGMENTATION_RESPONSE_BODY = getFileContent("http/response/segmentation-response-body.json");
	public static final String SEGMENTATION_EMPTY_RESPONSE_BODY = "{}";
	public static final String LOGIN_EVENT = getFileContent("kafka/login-event.json");
	private static final String CONFIGURATION_ENTITY = getFileContent("db/configuration/configuration-entity.json");
	private static final String CONFIGURATION_ENTITY_BEFORE_UPDATE = getFileContent("db/configuration/configuration-entity-before-update.json");
	private static final String USER_STATE = getFileContent("db/state/user-state.json");
	private static final String USER_STATE_WITH_INACTIVE_CONFIGURATION = getFileContent("db/state/user-state-with-inactive-configuration.json");
	private static final String USER_STATE_WITH_NEW_ACTIVE_CONFIGURATION = getFileContent("db/state/user-state-with-new-active-configuration.json");

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

	public static UserStateDocument userState(long version) {
		return userState(USER_STATE, version);
	}

	public static UserStateDocument userStateWithInactiveConfiguration(long version) {
		return userState(USER_STATE_WITH_INACTIVE_CONFIGURATION, version);
	}

	public static UserStateDocument userStateWithNewActiveConfiguration(long version) {
		return userState(USER_STATE_WITH_NEW_ACTIVE_CONFIGURATION, version);
	}

	@SneakyThrows
	private static UserStateDocument userState(String userState, long version) {
		var userStateDocument = OBJECT_MAPPER.readValue(userState, UserStateDocument.class);
		return userStateDocument.toBuilder().version(version).build();
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
