package functional.controller;

import static functional.FunctionalTestData.ALL_CONFIGURATIONS_RESPONSE_BODY;
import static functional.FunctionalTestData.CONFIGURATION_REQUEST_BODY;
import static functional.FunctionalTestData.INVALID_CONFIGURATION_REQUEST_BODY;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.configurationEntityBeforeUpdate;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.junit.jupiter.api.Test;

import functional.FunctionalTest;
import io.restassured.http.ContentType;

public class ConfigurationControllerTest extends FunctionalTest {

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
		var createdConfiguration = configurationRepository.findById(configurationId).get();
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
		assertThat(response).contains(
				"Validation failure",
				"with 9 errors",
				"Field error in object 'configurationDto' on field 'startTimestamp': rejected value [0]",
				"Field error in object 'configurationDto' on field 'endTimestamp': rejected value [-10]",
				"Error in object 'configurationDto': codes [TimeRangeValidation.configurationDto,TimeRangeValidation]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-1]': rejected value [{}]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].progressionTarget': rejected value [-1]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.unitId': rejected value [0]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.amount': rejected value [-200]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].progressionTarget': rejected value [0]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].reward.unitId': rejected value [0]"
		);

		// and
		assertThat(configurationRepository.findAll().isEmpty()).isTrue();
	}

	@Test
	void shouldUpdateConfiguration() {
		// given
		var configurationId = configurationRepository.save(configurationEntityBeforeUpdate()).getId();
		var configurationUri = CONFIGURATION_URI.formatted(configurationId);

		// when
		given().body(CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.put(configurationUri)
				.then()
				.statusCode(NO_CONTENT.value())
				.log().all();

		// then
		var updatedConfiguration = configurationRepository.findById(configurationId).get();
		var expectedUpdatedConfiguration = configurationEntity(configurationId);
		assertThat(updatedConfiguration).isEqualTo(expectedUpdatedConfiguration);
	}

	@Test
	void shouldNotUpdateConfiguration_ifRequestIsInvalid() {
		// given
		var configurationEntityBeforeUpdate = configurationRepository.save(configurationEntityBeforeUpdate());
		var configurationId = configurationEntityBeforeUpdate.getId();
		var configurationUri = CONFIGURATION_URI.formatted(configurationId);

		// when
		var response = given().body(INVALID_CONFIGURATION_REQUEST_BODY)
				.contentType(ContentType.JSON)
				.when()
				.put(configurationUri)
				.then()
				.statusCode(BAD_REQUEST.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).contains(
				"Validation failure",
				"with 9 errors",
				"Field error in object 'configurationDto' on field 'startTimestamp': rejected value [0]",
				"Field error in object 'configurationDto' on field 'endTimestamp': rejected value [-10]",
				"Error in object 'configurationDto': codes [TimeRangeValidation.configurationDto,TimeRangeValidation]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-1]': rejected value [{}]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].progressionTarget': rejected value [-1]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.unitId': rejected value [0]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_WON].reward.amount': rejected value [-200]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].progressionTarget': rejected value [0]",
				"Field error in object 'configurationDto' on field 'segmentedProgressionsConfiguration[segment-2][SOURCE_1_TOTAL].reward.unitId': rejected value [0]"
		);

		// and
		var configuration = configurationRepository.findById(configurationId).get();
		assertThat(configuration).isEqualTo(configurationEntityBeforeUpdate);
	}

	@Test
	void shouldGetAllConfiguration() {
		// given
		configurationRepository.saveAll(List.of(
				configurationEntity(),
				configurationEntityBeforeUpdate()
		));

		// when
		var response = when()
				.get(CONFIGURATIONS_URL)
				.then()
				.statusCode(OK.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo(ALL_CONFIGURATIONS_RESPONSE_BODY);
	}

	// todo: test no configurations were found
}
