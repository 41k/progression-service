package unit.root.infrastructure.consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static unit.TestData.USER_ID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import root.application.service.UserStateService;
import root.infrastructure.consumer.LoginEventConsumer;
import root.infrastructure.dto.LoginEvent;

@ExtendWith(MockitoExtension.class)
public class LoginEventConsumerTest {

	private static final LoginEvent EVENT = new LoginEvent(USER_ID);

	@Mock
	private UserStateService userStateService;
	@InjectMocks
	private LoginEventConsumer consumer;

	@Test
	void consume() {
		consumer.consume(EVENT);

		verify(userStateService).assignActivateConfigurationToUser(USER_ID);
		verifyNoMoreInteractions(userStateService);
	}

	@Test
	void consume_shouldSwallowException() {
		// given
		doThrow(new RuntimeException()).when(userStateService).assignActivateConfigurationToUser(USER_ID);

		// expect
		assertDoesNotThrow(() -> consumer.consume(EVENT));

		// and
		verify(userStateService).assignActivateConfigurationToUser(USER_ID);
		verifyNoMoreInteractions(userStateService);

	}
}
