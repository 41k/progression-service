package functional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import root.ApplicationRunner;
import root.infrastructure.persistence.configuration.ConfigurationRepository;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {ApplicationRunner.class, FunctionalTestConfiguration.class})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles({"test"})
public abstract class FunctionalTest {

	@LocalServerPort
	protected int serverPort;

	@Autowired
	protected ConfigurationRepository configurationRepository;

	@BeforeEach
	public void setUp() {
		RestAssured.port = serverPort;

		configurationRepository.truncate();
	}
}
