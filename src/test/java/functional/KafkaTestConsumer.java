package functional;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.awaitility.Durations.ONE_SECOND;
import static org.awaitility.Durations.TEN_SECONDS;
import static org.springframework.kafka.test.utils.KafkaTestUtils.getRecords;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.javacrumbs.jsonunit.core.Option;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;

public class KafkaTestConsumer implements Closeable {

	private static final String CONSUMER_GROUP_NAME_PREFIX = "test-consumer-group:";
	private static final String AUTO_OFFSET_RESET = "earliest";

	private final KafkaConsumer<String, String> consumer;

	public KafkaTestConsumer(String topic, String brokers) {
		var config = new HashMap<String, Object>() {{
			put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
			put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_NAME_PREFIX + topic);
			put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
			put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
			put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		}};
		consumer = new KafkaConsumer<>(config);
		consumer.subscribe(List.of(topic));
	}

	@Override
	public void close() {
		consumer.close();
	}

	public void assertMessageSent(Pair<String, String> expectedMessage) {
		assertMessagesSent(List.of(expectedMessage));
	}

	public void assertMessagesSent(List<Pair<String, String>> expectedMessages) {
		var actualMessages = receiveMessages(consumer, expectedMessages.size());
		for (int i = 0; i < expectedMessages.size(); i++) {
			var foundMatching = false;
			for (int j = 0; j < actualMessages.size(); j++) {
				var expectedMessage = expectedMessages.get(i);
				var actualMessage = actualMessages.get(i);
				foundMatching = expectedMessage.getKey().equals(actualMessage.getKey()) &&
						(expectedMessage.getValue().equals(actualMessage.getValue()) ||
								matchJson(expectedMessage.getValue(), actualMessage.getValue()));
				if (foundMatching) {
					break;
				}
			}
			assert foundMatching :
					"Expected messages are " + expectedMessages + ", but received messages are " + actualMessages;
		}
	}

	public void assertNoMessageSent() {
		assert getRecords(consumer, ONE_SECOND).count() == 0;
	}

	public void cleanupTopic() {
		getRecords(consumer, ONE_SECOND);
	}

	private boolean matchJson(String expected, String actual) {
		try {
			assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER).isEqualTo(expected);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private <K, V> List<Pair<K, V>> receiveMessages(KafkaConsumer<K, V> consumer, int nMessages) {
		var consumedMessages = new ArrayList<Pair<K, V>>();
		Awaitility.await()
				.atMost(TEN_SECONDS)
				.with()
				.pollInterval(ONE_SECOND)
				.until(() -> {
					getRecords(consumer, ONE_SECOND).forEach(record ->
							consumedMessages.add(Pair.of(record.key(), record.value())));
					return consumedMessages.size() >= nMessages;
				});
		return consumedMessages;
	}

}
