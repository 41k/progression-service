package functional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static functional.FunctionalTestData.SEGMENTATION_REQUEST_BODY;
import static functional.FunctionalTestData.SEGMENTATION_RESPONSE_BODY;
import static functional.FunctionalTestData.SEGMENTATION_URL;
import static org.apache.http.HttpStatus.SC_OK;
import static org.awaitility.Durations.ONE_SECOND;
import static org.awaitility.Durations.TEN_SECONDS;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import com.github.benmanes.caffeine.cache.Cache;

import io.restassured.RestAssured;
import root.ApplicationRunner;
import root.infrastructure.persistence.configuration.ConfigurationEntity;
import root.infrastructure.persistence.configuration.ConfigurationRepository;
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
	protected Cache<String, Map<Long, ConfigurationEntity>> cache;

	@Autowired
	protected ConfigurationRepository configurationRepository;

	@Autowired
	protected UserStateRepository userStateRepository;

	@Autowired
	private KafkaProducer kafkaProducer;

	@BeforeEach
	public void setUp() {
		RestAssured.port = serverPort;

		cache.invalidateAll();
		configurationRepository.truncate();
		userStateRepository.deleteAll();
	}

	protected void sendKafkaMessage(String topic, String message) {
		kafkaProducer.send(new ProducerRecord<String, String>(topic, null, message));
	}

	protected void mockCallToSegmentationService(String userSegment) {
		stubFor(post(urlPathEqualTo(SEGMENTATION_URL))
				.withRequestBody(equalToJson(SEGMENTATION_REQUEST_BODY, true, false))
				.willReturn(aResponse()
						.withStatus(SC_OK)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(SEGMENTATION_RESPONSE_BODY.formatted(userSegment))));
	}

	protected void awaitAndAssert(ThrowingRunnable assertion) {
		Awaitility.await()
				.atMost(TEN_SECONDS)
				.with()
				.pollInterval(ONE_SECOND)
				.untilAsserted(assertion);
	}
}
