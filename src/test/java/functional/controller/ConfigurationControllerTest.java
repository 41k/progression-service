package functional.controller;

import static functional.FunctionalTestData.CREATE_CONFIGURATION_REQUEST;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

import org.junit.jupiter.api.Test;

import functional.FunctionalTest;
import io.restassured.http.ContentType;

public class ConfigurationControllerTest extends FunctionalTest {

	private static final String CONFIGURATIONS_URL = "/progression-service/admin/v1/configurations";

	@Test
	void shouldCreateConfiguration() {
		// when
		var configurationId = given().body(CREATE_CONFIGURATION_REQUEST)
				.contentType(ContentType.JSON)
				.when()
				.post(CONFIGURATIONS_URL)
				.then()
				.statusCode(CREATED.value())
				.log().all()
				.extract().body().as(Long.class);

		// then
		var createdConfiguration = configurationRepository.findById(configurationId);
		assertThat(createdConfiguration.isPresent()).isTrue();
	}
}
