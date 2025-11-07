package functional.flow;

import static functional.FunctionalTestData.USER_ID;
import static functional.FunctionalTestData.USER_STATE_RESPONSE_BODY;
import static functional.FunctionalTestData.configurationEntity;
import static functional.FunctionalTestData.userState;
import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static root.infrastructure.controller.client.MeController.USER_ID_HEADER;

import org.junit.jupiter.api.Test;

import functional.FunctionalTest;

public class UserStateRetrievalFlowTest extends FunctionalTest {

	private static final String ME_URI = "/progression-service/api/public/v1/me";
	private static final String USER_STATE_NOT_FOUND_RESPONSE = "Resource is not found: Active user state is not found by id=" + USER_ID;

	@Test
	void shouldReturnUserState() {
		// given
		configurationRepository.saveAndFlush(configurationEntity());
		userStateRepository.save(userState(0));

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
		assertThatJson(response).isEqualTo(USER_STATE_RESPONSE_BODY);
	}

	@Test
	void shouldNotReturnUserState_ifThereIsNoActiveConfigurationLinkedToIt() {
		// given
		userStateRepository.save(userState(0));

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
