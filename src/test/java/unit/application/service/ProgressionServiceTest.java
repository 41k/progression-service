package unit.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.EVENT;
import static unit.UnitTestData.USER_ID;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import root.application.model.ProgressionUpdateTask;
import root.application.model.event.EventSource;
import root.application.service.ProgressionService;
import root.application.service.RewardService;
import root.application.service.UserStateService;
import root.application.service.progression.handler.ProgressionHandler;
import root.configuration.properties.EventSourceProperties;
import root.configuration.properties.ProgressionProperties;

@ExtendWith(MockitoExtension.class)
public class ProgressionServiceTest {

	private static final String PROGRESSION_HANDLER_1_NAME = "progressionHandler1";
	private static final String PROGRESSION_HANDLER_2_NAME = "progressionHandler2";

	@Mock
	private ProgressionHandler progressionHandler1;
	@Mock
	private ProgressionHandler progressionHandler2;
	@Mock
	private UserStateService userStateService;
	@Mock
	private RewardService rewardService;
	private ProgressionService progressionService;

	@BeforeEach
	void setUp() {
		ProgressionProperties progressionProperties = new ProgressionProperties(Map.of(
				EventSource.SOURCE_1,
				new EventSourceProperties(true, "source-1", new LinkedHashSet<>(List.of(PROGRESSION_HANDLER_1_NAME, PROGRESSION_HANDLER_2_NAME)))
		));
		Map<String, ProgressionHandler> progressionHandlers = Map.of(
				PROGRESSION_HANDLER_1_NAME, progressionHandler1,
				PROGRESSION_HANDLER_2_NAME, progressionHandler2
		);
		progressionService = new ProgressionService(progressionProperties, progressionHandlers, userStateService, rewardService);
	}

	@Test
	void process_shouldProcessEventSuccessfully() {
		// given
		ProgressionUpdateTask progressionUpdateTask = new ProgressionUpdateTask(EVENT, List.of(progressionHandler2));

		// and
		when(progressionHandler1.isEligible(EVENT)).thenReturn(false);
		when(progressionHandler2.isEligible(EVENT)).thenReturn(true);

		// when
		progressionService.process(EVENT);

		// then
		verify(progressionHandler1).isEligible(EVENT);
		verify(progressionHandler2).isEligible(EVENT);
		verify(userStateService).updateUserStateIfPresent(USER_ID, progressionUpdateTask);
		verify(rewardService).sendRewards(USER_ID, List.of());
	}

	@Test
	void process_shouldSkipEventProcessing_ifThereAreNoEligibleProgressionHandlers() {
		// given
		when(progressionHandler1.isEligible(EVENT)).thenReturn(false);
		when(progressionHandler2.isEligible(EVENT)).thenReturn(false);

		// when
		progressionService.process(EVENT);

		// then
		verify(progressionHandler1).isEligible(EVENT);
		verify(progressionHandler2).isEligible(EVENT);
		verifyNoInteractions(userStateService, rewardService);
	}

	@Test
	void process_shouldSwallowExceptionAndSkipEventProcessing() {
		// given
		when(progressionHandler1.isEligible(EVENT)).thenThrow(new RuntimeException());

		// when
		progressionService.process(EVENT);

		// then
		verify(progressionHandler1).isEligible(EVENT);
		verifyNoInteractions(progressionHandler2, userStateService, rewardService);
	}
}
