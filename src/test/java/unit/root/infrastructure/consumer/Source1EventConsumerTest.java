package unit.root.infrastructure.consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import root.application.model.event.Source1Event;
import root.application.service.ProgressionService;
import root.infrastructure.consumer.Source1EventConsumer;

@ExtendWith(MockitoExtension.class)
public class Source1EventConsumerTest {
	@Mock
	private ProgressionService progressionService;
	@InjectMocks
	private Source1EventConsumer consumer;

	@Test
	void shouldConsumeEvent() {
		var event = new Source1Event();

		consumer.consume(event);

		verify(progressionService).process(event);
		verifyNoMoreInteractions(progressionService);
	}
}
