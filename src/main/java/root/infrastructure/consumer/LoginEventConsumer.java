package root.infrastructure.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.service.UserStateService;
import root.infrastructure.dto.LoginEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginEventConsumer {

	private final UserStateService userStateService;

	@KafkaListener(
			topics = "${login-topic}",
			concurrency = "1",
			properties = "spring.json.value.default.type=root.infrastructure.dto.LoginEvent"
	)
	public void consume(LoginEvent event) {
		try {
			log.debug("Received {}", event);
			userStateService.assignActivateConfigurationToUser(event.userId());
		} catch (Exception e) {
			log.error("Failed to process {}", event, e);
		}
	}
}
