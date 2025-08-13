package unit.root.application.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static unit.TestData.EVENT;
import static unit.TestData.USER_ID;
import static unit.TestData.USER_STATE;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	private ProgressionProperties progressionProperties;
	private Map<String, ProgressionHandler> progressionHandlers;
	private ProgressionUpdateTask progressionUpdateTask;
	private ProgressionService progressionService;

	@BeforeEach
	void setUp() {
		progressionProperties = new ProgressionProperties(Map.of(
				EventSource.SOURCE_1,
				new EventSourceProperties(true, "source-1", new LinkedHashSet<>(List.of(PROGRESSION_HANDLER_1_NAME, PROGRESSION_HANDLER_2_NAME)))
		));
		progressionHandlers = Map.of(
				PROGRESSION_HANDLER_1_NAME, progressionHandler1,
				PROGRESSION_HANDLER_2_NAME, progressionHandler2
		);
		progressionUpdateTask = new ProgressionUpdateTask(EVENT, List.of(progressionHandler1, progressionHandler2));
		progressionService = new ProgressionService(progressionProperties, progressionHandlers, userStateService, rewardService);
	}

	@Test
	void shouldProcessEventSuccessfully() {
		// given
		when(progressionHandler1.isEligible(EVENT)).thenReturn(true);
		when(progressionHandler2.isEligible(EVENT)).thenReturn(true);
		when(userStateService.updateUserStateIfPresent(USER_ID, progressionUpdateTask)).thenReturn(Optional.of(USER_STATE));

		// when
		progressionService.process(EVENT);

		// then
		verify(progressionHandler1).isEligible(EVENT);
		verify(progressionHandler2).isEligible(EVENT);
		verify(userStateService).updateUserStateIfPresent(USER_ID, progressionUpdateTask);
		verify(rewardService).sendRewards(USER_STATE);
	}

	@Test
	void shouldSkipEventProcessing_ifUserStateIsNotFound() {
		// given
		when(progressionHandler1.isEligible(EVENT)).thenReturn(true);
		when(progressionHandler2.isEligible(EVENT)).thenReturn(true);
		when(userStateService.updateUserStateIfPresent(USER_ID, progressionUpdateTask)).thenReturn(Optional.empty());

		// when
		progressionService.process(EVENT);

		// then
		verify(progressionHandler1).isEligible(EVENT);
		verify(progressionHandler2).isEligible(EVENT);
		verify(userStateService).updateUserStateIfPresent(USER_ID, progressionUpdateTask);
		verifyNoInteractions(rewardService);
	}

	@Test
	void shouldSkipEventProcessing_ifThereAreNoEligibleProgressionHandlers() {
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
	void shouldSwallowExceptionAndSkipEventProcessing() {
		// given
		when(progressionHandler1.isEligible(EVENT)).thenThrow(new RuntimeException());

		// when
		progressionService.process(EVENT);

		// then
		verify(progressionHandler1).isEligible(EVENT);
		verifyNoInteractions(progressionHandler2, userStateService, rewardService);
	}
}
