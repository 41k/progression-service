package functional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.state.UserStateDocument;

@UtilityClass
public class FunctionalTestData {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Collector<CharSequence, ?, String> LINES_COLLECTOR = Collectors.joining("\n");

	public static final String USER_ID = "1000";
	public static final String SEGMENT_1 = "segment-1";
	public static final Long CURRENT_TIMESTAMP = 600L;

	public static final String SEGMENTATION_URL = "/segmentation-service/api/v1/evaluate";

	public static final ConfigurationRequest CONFIGURATION_REQUEST_BODY = deserialize("http/request/configuration-request-body.json", ConfigurationRequest.class);
	public static final String CONFIGURATION_RESPONSE_BODY = getFileContent("http/response/configuration-response-body.json");
	public static final String CONFIGURATIONS_RESPONSE_BODY = getFileContent("http/response/configurations-response-body.json");
	public static final String EMPTY_PAGINATED_RESPONSE_BODY = getFileContent("http/response/empty-paginated-response-body.json");
	public static final String USER_STATE_RESPONSE_BODY = getFileContent("http/response/user-state-response-body.json");
	public static final String SEGMENTATION_REQUEST_BODY = getFileContent("http/request/segmentation-request-body.json");
	public static final String SEGMENTATION_RESPONSE_BODY = getFileContent("http/response/segmentation-response-body.json");
	public static final String SEGMENTATION_EMPTY_RESPONSE_BODY = "{}";
	public static final String LOGIN_EVENT = getFileContent("kafka/login-event.json");
	public static final String SOURCE_1_EVENT_FOR_TOTAL_PROGRESSION = getFileContent("kafka/source-1-event-for-total-progression.json");
	public static final String SOURCE_1_EVENT_FOR_WON_PROGRESSION = getFileContent("kafka/source-1-event-for-won-progression.json");
	public static final String SOURCE_2_EVENT = getFileContent("kafka/source-2-event.json");
	public static final String SOURCE_1_PROGRESSION_REWARD_MESSAGE = getFileContent("kafka/source-1-progression-reward-message.json");
	public static final String SOURCE_2_PROGRESSION_REWARD_MESSAGE = getFileContent("kafka/source-2-progression-reward-message.json");
	private static final String CONFIGURATION_ENTITY = getFileContent("db/configuration/configuration-entity.json");
	private static final String CONFIGURATION_ENTITY_BEFORE_UPDATE = getFileContent("db/configuration/configuration-entity-before-update.json");
	private static final String USER_STATE = getFileContent("db/state/user-state.json");
	private static final String USER_STATE_WITH_INACTIVE_CONFIGURATION = getFileContent("db/state/user-state-with-inactive-configuration.json");
	private static final String USER_STATE_WITH_NEW_ACTIVE_CONFIGURATION = getFileContent("db/state/user-state-with-new-active-configuration.json");
	private static final String USER_STATE_WITH_ACTIVE_OUTDATED_CONFIGURATION = getFileContent("db/state/user-state-with-active-outdated-configuration.json");

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
	public static UserStateDocument userState(long version) {
		return OBJECT_MAPPER.readValue(USER_STATE, UserStateDocument.class).toBuilder().version(version).build();
	}

	@SneakyThrows
	public static UserStateDocument userStateWithInactiveConfiguration(long version) {
		return OBJECT_MAPPER.readValue(USER_STATE_WITH_INACTIVE_CONFIGURATION, UserStateDocument.class)
				.toBuilder().version(version).build();
	}

	@SneakyThrows
	public static UserStateDocument userStateWithNewActiveConfiguration(long version) {
		return OBJECT_MAPPER.readValue(USER_STATE_WITH_NEW_ACTIVE_CONFIGURATION, UserStateDocument.class)
				.toBuilder().version(version).build();
	}

	@SneakyThrows
	public static UserStateDocument userStateWithActiveOutdatedConfiguration() {
		return OBJECT_MAPPER.readValue(USER_STATE_WITH_ACTIVE_OUTDATED_CONFIGURATION, UserStateDocument.class);
	}

	@SneakyThrows
	private static String getFileContent(String filePath) {
		var path = Paths.get(FunctionalTestData.class.getClassLoader().getResource(filePath).toURI());
		try (var lines = Files.lines(path)) {
			return lines.collect(LINES_COLLECTOR);
		} catch (Exception e) {
			throw new IOException("Failed to load file: %s".formatted(filePath), e);
		}
	}

	@SneakyThrows
	private static <T> T deserialize(String filePath, Class<T> type) {
		String fileContent = getFileContent(filePath);
		return OBJECT_MAPPER.readValue(fileContent, type);
	}
}
