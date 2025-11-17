package functional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static functional.FunctionalTestData.SEGMENTATION_EMPTY_RESPONSE_BODY;
import static functional.FunctionalTestData.SEGMENTATION_REQUEST_BODY;
import static functional.FunctionalTestData.SEGMENTATION_RESPONSE_BODY;
import static functional.FunctionalTestData.SEGMENTATION_URL;
import static functional.FunctionalTestData.USER_ID;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Durations.ONE_SECOND;
import static org.awaitility.Durations.TEN_SECONDS;
import static org.awaitility.Durations.TWO_SECONDS;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Map;

import net.javacrumbs.jsonunit.core.Option;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.matcher.AssertionMatcher;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import com.github.benmanes.caffeine.cache.Cache;

import io.restassured.RestAssured;
import lombok.SneakyThrows;
import root.ApplicationRunner;
import root.application.model.Configuration;
import root.application.model.ProgressionType;
import root.infrastructure.persistence.configuration.ConfigurationRepository;
import root.infrastructure.persistence.state.UserStateDocument;
import root.infrastructure.persistence.state.UserStateRepository;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {ApplicationRunner.class, FunctionalTestConfiguration.class})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles({"test"})
public abstract class FunctionalTest {

	@LocalServerPort
	protected int serverPort;

	@Autowired
	protected Cache<String, Map<Long, Configuration>> cache;

	@Autowired
	protected ConfigurationRepository configurationRepository;

	@Autowired
	protected UserStateRepository userStateRepository;

	@Autowired
	protected KafkaTestConsumer rewardsTopicConsumer;

	@Autowired
	private KafkaProducer<String, String> kafkaProducer;

	@BeforeEach
	public void setUp() {
		RestAssured.port = serverPort;

		cache.invalidateAll();

		configurationRepository.truncate();
		userStateRepository.deleteAll();

		rewardsTopicConsumer.cleanupTopic();
	}

	protected void sendKafkaMessage(String topic, String message) {
		kafkaProducer.send(new ProducerRecord<>(topic, null, message));
	}

	protected void mockCallToSegmentationService(String userSegment) {
		var responseBody = userSegment == null ?
				SEGMENTATION_EMPTY_RESPONSE_BODY :
				SEGMENTATION_RESPONSE_BODY.formatted(userSegment);
		stubFor(post(urlPathEqualTo(SEGMENTATION_URL))
				.withRequestBody(equalToJson(SEGMENTATION_REQUEST_BODY, true, false))
				.willReturn(aResponse()
						.withStatus(SC_OK)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(responseBody)));
	}

	protected void assertUserStateWithPolling(UserStateDocument expectedUserState) {
		assertWithPolling(() -> assertUserState(expectedUserState));
	}

	@SneakyThrows
	protected void waitAndAssertUserState(UserStateDocument expectedUserState) {
		Thread.sleep(TWO_SECONDS);
		assertUserState(expectedUserState);
	}

	protected void assertUserProgressionWithPolling(ProgressionType progressionType, long expectedProgressionValue) {
		assertWithPolling(() -> assertUserProgression(progressionType, expectedProgressionValue));
	}

	protected void assertWithPolling(ThrowingRunnable assertion) {
		Awaitility.await()
				.atMost(TEN_SECONDS)
				.with()
				.pollInterval(ONE_SECOND)
				.untilAsserted(assertion);
	}

	protected void assertUserState(UserStateDocument expectedUserState) {
		var userState = userStateRepository.findById(USER_ID).orElseThrow();
		assertThat(userState).isEqualTo(expectedUserState);
	}

	protected void assertUserProgression(ProgressionType progressionType, Long expectedProgressionValue) {
		var userState = userStateRepository.findById(USER_ID).orElseThrow();
		var progressionValue = userState.getProgressions().get(progressionType);
		assertThat(progressionValue).isEqualTo(expectedProgressionValue);
	}

	protected Matcher<String> equalsToJson(String expectedJson) {
		return new AssertionMatcher<>() {
			public void assertion(String actualJson) {
				assertThatJson(actualJson)
						.when(Option.IGNORING_ARRAY_ORDER)
						.isEqualTo(expectedJson);
			}
		};
	}
}
