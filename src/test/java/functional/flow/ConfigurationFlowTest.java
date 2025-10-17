package functional.flow;

import static functional.FunctionalTestData.ALL_CONFIGURATIONS_RESPONSE_BODY;
import static functional.FunctionalTestData.CONFIGURATION_ENTITY;
import static functional.FunctionalTestData.CONFIGURATION_ENTITY_BEFORE_UPDATE;
import static functional.FunctionalTestData.CONFIGURATION_REQUEST_BODY;
import static functional.FunctionalTestData.CONFIGURATION_RESPONSE_BODY;
import static functional.FunctionalTestData.INVALID_CONFIGURATION_REQUEST_BODY;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestUtils.jsonMatch;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.junit.jupiter.api.Test;

import functional.FunctionalTest;
import io.restassured.http.ContentType;

public class ConfigurationFlowTest extends FunctionalTest {

	private static final String CONFIGURATIONS_URL = "/progression-service/admin/v1/configurations";
	private static final String CONFIGURATION_URI = CONFIGURATIONS_URL + "/%d";

	@Test
	void shouldCreateConfiguration() {
		// when
		var configurationId = given().body(CONFIGURATION_REQUEST_BODY)
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

	@Test
	void shouldNotCreateConfiguration_ifRequestIsInvalid() {
		// given
		assertThat(configurationRepository.findAll().isEmpty()).isTrue();

		// when
		var response = given().body(INVALID_CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.post(CONFIGURATIONS_URL)
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).contains( // todo: refactor to approach with @ParameterizedTest
				"Validation failure",
				"with 9 errors",
				"Field error in object 'configurationRequest' on field 'startTimestamp': rejected value [0]",
				"Field error in object 'configurationRequest' on field 'endTimestamp': rejected value [-10]",
				"Error in object 'configurationRequest': codes [TimeRangeValidation.configurationRequest,TimeRangeValidation]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-1]': rejected value [{}]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].progressionTarget': rejected value [-1]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.unitId': rejected value [0]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.amount': rejected value [-200]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].progressionTarget': rejected value [0]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].reward.unitId': rejected value [0]"
		);

		// and
		assertThat(configurationRepository.findAll().isEmpty()).isTrue();
	}

	@Test
	void shouldUpdateConfiguration() {
		// given
		var configurationId = configurationRepository.save(CONFIGURATION_ENTITY_BEFORE_UPDATE).getId();

		// when
		given().body(CONFIGURATION_REQUEST_BODY)
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

	@Test
	void shouldNotUpdateConfiguration_ifRequestIsInvalid() {
		// given
		var configurationEntityBeforeUpdate = configurationRepository.save(CONFIGURATION_ENTITY_BEFORE_UPDATE);
		var configurationId = configurationEntityBeforeUpdate.getId();

		// when
		var response = given().body(INVALID_CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.put(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).contains( // todo: refactor to approach with @ParameterizedTest
				"Validation failure",
				"with 9 errors",
				"Field error in object 'configurationRequest' on field 'startTimestamp': rejected value [0]",
				"Field error in object 'configurationRequest' on field 'endTimestamp': rejected value [-10]",
				"Error in object 'configurationRequest': codes [TimeRangeValidation.configurationRequest,TimeRangeValidation]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-1]': rejected value [{}]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].progressionTarget': rejected value [-1]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.unitId': rejected value [0]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.amount': rejected value [-200]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].progressionTarget': rejected value [0]",
				"Field error in object 'configurationRequest' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].reward.unitId': rejected value [0]"
		);

		// and
		var configuration = configurationRepository.findById(configurationId).orElseThrow();
		assertThat(configuration).isEqualTo(configurationEntityBeforeUpdate);
	}

	@Test
	void shouldReturnAllConfigurations() {
		// given
		configurationRepository.saveAllAndFlush(List.of(
				CONFIGURATION_ENTITY_BEFORE_UPDATE,
				CONFIGURATION_ENTITY
		));

		// when
		var response = when()
				.get(CONFIGURATIONS_URL)
				.then()
				.statusCode(OK.value())
				.log().all()
				// todo: replace with .body(jsonMatcher(...)); here and in all other places
				.extract().body().asString();

		// then
		assertThat(jsonMatch(response, ALL_CONFIGURATIONS_RESPONSE_BODY)).isTrue();
	}

	@Test
	void shouldReturnConfiguration() {
		// given
		var configurationId = configurationRepository.saveAndFlush(CONFIGURATION_ENTITY).getId();

		// when
		var response = when()
				.get(CONFIGURATION_URI.formatted(configurationId))
				.then()
				.statusCode(OK.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(jsonMatch(response, CONFIGURATION_RESPONSE_BODY)).isTrue();
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
		var configurationId = configurationRepository.save(CONFIGURATION_ENTITY).getId();

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
}
