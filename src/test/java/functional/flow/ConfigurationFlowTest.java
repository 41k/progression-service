package functional.flow;

import static functional.FunctionalTestData.CONFIGURATIONS_RESPONSE_BODY;
import static functional.FunctionalTestData.CONFIGURATION_REQUEST_BODY;
import static functional.FunctionalTestData.CONFIGURATION_RESPONSE_BODY;
import static functional.FunctionalTestData.EMPTY_PAGINATED_RESPONSE_BODY;
import static functional.FunctionalTestData.SEGMENT_1;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.configurationEntityBeforeUpdate;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static root.application.model.ProgressionType.SOURCE_1_WON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
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
import root.infrastructure.persistence.configuration.ConfigurationEntity;

public class ConfigurationFlowTest extends FunctionalTest {

	private static final String CONFIGURATIONS_URL = "/progression-service/api/admin/v1/configurations";
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

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidConfigurationRequests")
	void shouldNotCreateConfiguration_ifRequestIsInvalid(String testCaseName,
	                                                     ConfigurationRequest invalidRequest,
	                                                     String... validationFailureReasons) {
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
	void shouldNotCreateConfiguration_ifTimeRangeIntersectsWithOtherExistingConfigurations() {
		// given
		configurationRepository.save(configurationEntity());

		// when
		var response = given()
				.body(CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.post(CONFIGURATIONS_URL)
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo("Validation failure: Configuration time range intersects with another existing configuration");
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

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidConfigurationRequests")
	void shouldNotUpdateConfiguration_ifRequestIsInvalid(String testCaseName,
	                                                     ConfigurationRequest invalidRequest,
	                                                     String... validationFailureReasons) {
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
	void shouldNotUpdateConfiguration_ifTimeRangeIntersectsWithOtherExistingConfigurations() {
		// given
		configurationRepository.save(configurationEntity());

		// when
		var response = given()
				.body(CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.put(CONFIGURATION_URI.formatted(100))
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo("Validation failure: Configuration time range intersects with another existing configuration");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("configurationsRetrievalRequests")
	void shouldReturnConfigurations(String testName, String requestParams, String responseBody) {
		// given
		var configurations = new ArrayList<ConfigurationEntity>();
		IntStream.range(1, 10).forEach(i -> {
			var startTimestamp = 10L * i;
			var endTimestamp = 11L * i;
			configurations.add(configurationEntity().toBuilder().name("group-a-config-" + i).startTimestamp(startTimestamp).endTimestamp(endTimestamp).build());
			configurations.add(configurationEntity().toBuilder().name("group-b-config-" + i).startTimestamp(startTimestamp).endTimestamp(endTimestamp).build());
		});
		configurationRepository.saveAllAndFlush(configurations);

		// expect
		when()
				.get(CONFIGURATIONS_URL + requestParams)
				.then()
				.statusCode(OK.value())
				.log().all()
				.body(equalsToJson(responseBody));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidConfigurationsRetrievalRequests")
	void shouldNotReturnConfigurations_ifRequestIsNotValid(String testCaseName, String requestParams, String validationFailureReason) {
		// when
		String response = when()
				.get(CONFIGURATIONS_URL + requestParams)
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).contains(validationFailureReason);
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

		// when
		var response = when()
				.get(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(NOT_FOUND.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo("Resource is not found: Configuration is not found by id=" + configurationId);
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
						"name is null",
						CONFIGURATION_REQUEST_BODY.toBuilder().name(null).build(),
						new String[] {"on field 'name': rejected value [null]", "must not be blank"}
				),
				Arguments.of(
						"name is empty string",
						CONFIGURATION_REQUEST_BODY.toBuilder().name(EMPTY).build(),
						new String[] {"on field 'name': rejected value []", "must not be blank"}
				),
				Arguments.of(
						"startTimestamp is not positive",
						CONFIGURATION_REQUEST_BODY.toBuilder().startTimestamp(-1).build(),
						new String[] {"on field 'startTimestamp': rejected value [-1]", "must be greater than 0"}
				),
				Arguments.of(
						"endTimestamp is not positive",
						CONFIGURATION_REQUEST_BODY.toBuilder().endTimestamp(-1).build(),
						new String[] {"on field 'endTimestamp': rejected value [-1]", "must be greater than 0"}
				),
				Arguments.of(
						"segmentedProgressionsConfiguration is null",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(null).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration': rejected value [null]", "must not be empty"}
				),
				Arguments.of(
						"segmentedProgressionsConfiguration is empty map",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of()).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration': rejected value [{}]", "must not be empty"}
				),
				Arguments.of(
						"progressionConfigurationPerType is empty map",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of())).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1]': rejected value [{}]", "must not be empty"}
				),
				Arguments.of(
						"progressionConfiguration is null",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, noProgressionConfigurationPerType)).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON]': rejected value [null]", "must not be null"}
				),
				Arguments.of(
						"progressionTarget is less than 1",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().progressionTarget(0).build()))).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].progressionTarget': rejected value [0]", "must be greater than or equal to 1"}
				),
				Arguments.of(
						"reward is null",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().reward(null).build()))).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].reward': rejected value [null]", "must not be null"}
				),
				Arguments.of(
						"reward.unitId is not positive",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().reward(new RewardDto(0, 1)).build()))).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].reward.unitId': rejected value [0]", "must be greater than 0"}
				),
				Arguments.of(
						"reward.amount is not positive",
						CONFIGURATION_REQUEST_BODY.toBuilder().segmentedProgressionsConfiguration(Map.of(SEGMENT_1, Map.of(SOURCE_1_WON, progressionConfiguration.toBuilder().reward(new RewardDto(1, 0)).build()))).build(),
						new String[] {"on field 'segmentedProgressionsConfiguration[segment-1][SOURCE_1_WON].reward.amount': rejected value [0]", "must be greater than 0"}
				)
		);
	}

	static Stream<Arguments> configurationsRetrievalRequests() {
		return Stream.of(
				Arguments.of(
						"several configurations match filter",
						"?pageNumber=1&pageSize=2&orderBy=id&order=desc&name=a-config&startTimestamp=30&endTimestamp=90",
						CONFIGURATIONS_RESPONSE_BODY
				),
				Arguments.of(
						"no configurations match filter",
						"?pageNumber=0&pageSize=1&orderBy=id&order=desc&name=non-existent-name",
						EMPTY_PAGINATED_RESPONSE_BODY
				)
		);
	}

	static Stream<Arguments> invalidConfigurationsRetrievalRequests() {
		return Stream.of(
				Arguments.of(
						"pageNumber is not provided",
						"?pageSize=2&orderBy=id&order=asc",
						"Required request parameter 'pageNumber' for method parameter type Integer is not present"
				),
				Arguments.of(
						"pageNumber is negative",
						"?pageNumber=-1&pageSize=2&orderBy=id&order=asc",
						"getConfigurations.pageNumber: must be greater than or equal to 0"
				),
				Arguments.of(
						"pageSize is not provided",
						"?pageNumber=1&orderBy=id&order=asc",
						"Required request parameter 'pageSize' for method parameter type Integer is not present"
				),
				Arguments.of(
						"pageSize is less than 1",
						"?pageNumber=1&pageSize=0&orderBy=id&order=asc",
						"getConfigurations.pageSize: must be greater than or equal to 1"
				),
				Arguments.of(
						"orderBy is not provided",
						"?pageNumber=1&pageSize=2&order=asc",
						"Required request parameter 'orderBy' for method parameter type String is not present"
				),
				Arguments.of(
						"orderBy is empty string",
						"?pageNumber=1&pageSize=2&orderBy=&order=asc",
						"getConfigurations.orderBy: must not be blank"
				),
				Arguments.of(
						"order is not provided",
						"?pageNumber=1&pageSize=2&orderBy=id",
						"Required request parameter 'order' for method parameter type String is not present"
				),
				Arguments.of(
						"order is invalid",
						"?pageNumber=1&pageSize=2&orderBy=id&order=whatever",
						"getConfigurations.order: must match \"(?i)^asc|desc$\""
				)
		);
	}
}
