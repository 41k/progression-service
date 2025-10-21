package functional.flow;

import static functional.FunctionalTestData.ALL_CONFIGURATIONS_RESPONSE_BODY;
import static functional.FunctionalTestData.CONFIGURATION_REQUEST_BODY;
import static functional.FunctionalTestData.CONFIGURATION_RESPONSE_BODY;
import static functional.FunctionalTestData.SEGMENT_1;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.configurationEntityBeforeUpdate;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static root.application.model.ProgressionType.SOURCE_1_WON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import functional.FunctionalTest;
import io.restassured.http.ContentType;
import root.application.model.ProgressionType;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ProgressionConfigurationDto;
import root.infrastructure.dto.RewardDto;

public class ConfigurationFlowTest extends FunctionalTest {

	private static final String CONFIGURATIONS_URL = "/progression-service/admin/v1/configurations";
	private static final String CONFIGURATION_URI = CONFIGURATIONS_URL + "/%d";

	@Test
	void shouldCreateConfiguration() {
		// when
		var configurationId = given()
				.body(CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.post(CONFIGURATIONS_URL)
				.then()
				.statusCode(CREATED.value())
				.log().all()
				.extract().body().as(Long.class);

		// then
		var createdConfiguration = configurationRepository.findById(configurationId).orElseThrow();
		var expectedCreatedConfiguration = configurationEntity(configurationId);
		assertThat(createdConfiguration).isEqualTo(expectedCreatedConfiguration);
	}

	@ParameterizedTest
	@MethodSource("invalidConfigurationRequests")
	void shouldNotCreateConfiguration_ifRequestIsInvalid(ConfigurationRequest invalidRequest, String... validationFailureReasons) {
		// given
		assertThat(configurationRepository.findAll().isEmpty()).isTrue();

		// when
		var response = given()
				.body(invalidRequest)
				.contentType(ContentType.JSON)
				.when()
				.post(CONFIGURATIONS_URL)
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).contains("Validation failure");
		assertThat(response).contains(validationFailureReasons);

		// and
		assertThat(configurationRepository.findAll().isEmpty()).isTrue();
	}

	@Test
	void shouldUpdateConfiguration() {
		// given
		var configurationId = configurationRepository.save(configurationEntityBeforeUpdate()).getId();

		// when
		given()
				.body(CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.put(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(NO_CONTENT.value())
				.log().all();

		// then
		var updatedConfiguration = configurationRepository.findById(configurationId).orElseThrow();
		var expectedUpdatedConfiguration = configurationEntity(configurationId);
		assertThat(updatedConfiguration).isEqualTo(expectedUpdatedConfiguration);
	}

	@ParameterizedTest
	@MethodSource("invalidConfigurationRequests")
	void shouldNotUpdateConfiguration_ifRequestIsInvalid(ConfigurationRequest invalidRequest, String... validationFailureReasons) {
		// given
		var configurationEntityBeforeUpdate = configurationRepository.save(configurationEntityBeforeUpdate());
		var configurationId = configurationEntityBeforeUpdate.getId();

		// when
		var response = given()
				.body(invalidRequest)
				.contentType(ContentType.JSON)
				.when()
				.put(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).contains("Validation failure");
		assertThat(response).contains(validationFailureReasons);

		// and
		var configuration = configurationRepository.findById(configurationId).orElseThrow();
		assertThat(configuration).isEqualTo(configurationEntityBeforeUpdate);
	}

	@Test
	void shouldReturnAllConfigurations() {
		// given
		configurationRepository.saveAllAndFlush(List.of(
				configurationEntityBeforeUpdate(),
				configurationEntity()
		));

		// expect
		when()
				.get(CONFIGURATIONS_URL)
				.then()
				.statusCode(OK.value())
				.log().all()
				.body(equalsToJson(ALL_CONFIGURATIONS_RESPONSE_BODY));
	}

	@Test
	void shouldReturnConfiguration() {
		// given
		var configurationId = configurationRepository.saveAndFlush(configurationEntity()).getId();

		// expect
		when()
				.get(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(OK.value())
				.log().all()
				.body(equalsToJson(CONFIGURATION_RESPONSE_BODY));
	}

	@Test
	void shouldNotReturnConfiguration_ifItIsNotFound() {
		// given
		var configurationId = 1;
		var expectedResponse = "Resource is not found: Configuration is not found by id=" + configurationId;

		// when
		var response = when()
				.get(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(NOT_FOUND.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo(expectedResponse);
	}

	@Test
	void shouldDeleteConfiguration() {
		// given
		var configurationId = configurationRepository.save(configurationEntity()).getId();

		// and
		assertThat(configurationRepository.count()).isEqualTo(1);

		// when
		when()
				.delete(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(NO_CONTENT.value())
				.log().all();

		// then
		assertThat(configurationRepository.count()).isEqualTo(0);
	}

	static Stream<Arguments> invalidConfigurationRequests() {
		var noProgressionConfigurationPerType = new HashMap<ProgressionType, ProgressionConfigurationDto>() {{
			put(SOURCE_1_WON, null);
		}};
		var progressionConfiguration = CONFIGURATION_REQUEST_BODY.segmentedProgressionsConfiguration().get(SEGMENT_1).get(SOURCE_1_WON);
		return Stream.of(
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().startTimestamp(-1).build(),
						new String[] {"on field 'startTimestamp': rejected value [-1]", "must be greater than 0"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().endTimestamp(-1).build(),
						new String[] {"on field 'endTimestamp': rejected value [-1]", "must be greater than 0"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(null).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration': rejected value [null]", "must not be empty"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of()).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration': rejected value [{}]", "must not be empty"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of())).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1]': rejected value [{}]", "must not be empty"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, noProgressionConfigurationPerType)).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON]': rejected value [null]", "must not be null"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().progressionTarget(0).build()))).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].progressionTarget': rejected value [0]", "must be greater than or equal to 1"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().reward(null).build()))).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].reward': rejected value [null]", "must not be null"}
				),
				Arguments.of(
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().reward(new RewardDto(0, 0)).build()))).build(),
						new String[] {
								"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].reward.unitId': rejected value [0]", "must be greater than 0",
								"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].reward.amount': rejected value [0]", "must be greater than 0"
						}
				)
		);
	}
}
