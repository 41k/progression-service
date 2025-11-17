package functional;

import static functional.FunctionalTestData.CURRENT_TIMESTAMP;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FunctionalTestConfiguration {

	@Value("${embedded.kafka.brokerList}")
	private String kafkaBrokers;

	@Bean
	public Clock clock() {
		return Clock.fixed(Instant.ofEpochMilli(CURRENT_TIMESTAMP), ZoneId.systemDefault());
	}

	@Bean
	public KafkaProducer<String, String> kafkaProducer() {
		return new KafkaProducer<>(Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers,
				ProducerConfig.CLIENT_ID_CONFIG, "kafka-producer-1",
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
		));
	}

	@Bean(destroyMethod = "close")
	public KafkaTestConsumer rewardsTopicConsumer(@Value("${rewards-topic}") String topic) {
		return new KafkaTestConsumer(topic, kafkaBrokers);
	}
}
