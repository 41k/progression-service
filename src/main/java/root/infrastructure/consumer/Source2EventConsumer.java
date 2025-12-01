package root.infrastructure.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.event.Source2Event;
import root.application.service.ProgressionService;

@Component
@ConditionalOnExpression("${progression.sources.SOURCE_2.enabled:false}")
@Slf4j
@RequiredArgsConstructor
public class Source2EventConsumer {

	private final ProgressionService progressionService;

	@KafkaListener(
			topics = "${progression.sources.SOURCE_2.topic}",
			concurrency = "1",
			properties = "spring.json.value.default.type=root.application.model.event.Source2Event"
	)
	public void consume(@Payload Source2Event event) {
		log.debug("Received {}", event);
		progressionService.process(event);
	}
}
