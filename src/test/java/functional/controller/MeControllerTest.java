package functional.controller;

import static functional.FunctionalTestData.USER_ID;
import static functional.FunctionalTestData.USER_STATE_RESPONSE_BODY;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userState;
import static functional.FunctionalTestUtils.jsonMatch;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static root.infrastructure.controller.client.MeController.USER_ID_HEADER;

import org.junit.jupiter.api.Test;

import functional.FunctionalTest;

public class MeControllerTest extends FunctionalTest {

	private static final String ME_URI = "/progression-service/public/v1/me";
	private static final String USER_STATE_NOT_FOUND_RESPONSE = "Resource is not found: Active user state is not found by id=" + USER_ID;

	@Test
	void shouldReturnUserState() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userState());

		// when
		var response = given()
				.header(USER_ID_HEADER, USER_ID)
				.when()
				.get(ME_URI)
				.then()
				.statusCode(OK.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(jsonMatch(response, USER_STATE_RESPONSE_BODY)).isTrue();
	}

	@Test
	void shouldNotReturnUserState_ifThereIsNoActiveConfigurationLinkedToIt() {
		// given
		userStateRepository.save(userState());

		// when
		var response = given()
				.header(USER_ID_HEADER, USER_ID)
				.when()
				.get(ME_URI)
				.then()
				.statusCode(NOT_FOUND.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo(USER_STATE_NOT_FOUND_RESPONSE);
	}

	@Test
	void shouldNotReturnUserState_ifItIsNotFound() {
		// when
		var response = given()
				.header(USER_ID_HEADER, USER_ID)
				.when()
				.get(ME_URI)
				.then()
				.statusCode(NOT_FOUND.value())
				.log().all()
				.extract().body().asString();

		// then
		assertThat(response).isEqualTo(USER_STATE_NOT_FOUND_RESPONSE);
	}
}
