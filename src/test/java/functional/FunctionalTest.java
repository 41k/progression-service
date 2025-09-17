package functional;

import java.util.Map;

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

	@BeforeEach
	public void setUp() {
		RestAssured.port = serverPort;

		cache.invalidateAll();
		configurationRepository.truncate();
		userStateRepository.deleteAll();
	}
}
