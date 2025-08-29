package unit.root.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static unit.TestData.CONFIGURATION;
import static unit.TestData.CONFIGURATION_ID;
import static unit.TestData.CONFIGURATION_UPDATE_TIMESTAMP;
import static unit.TestData.PROGRESSIONS_CONFIGURATION_2;
import static unit.TestData.SEGMENTS;
import static unit.TestData.SEGMENT_2;
import static unit.TestData.USER_ID;
import static unit.TestData.USER_STATE;

import java.time.Clock;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.support.RetryTemplate;

import root.application.model.ProgressionType;
import root.application.model.UserConfiguration;
import root.application.model.UserState;
import root.application.service.ConfigurationService;
import root.application.service.SegmentationService;
import root.application.service.UserStatePersistenceService;
import root.application.service.UserStateService;

@ExtendWith(MockitoExtension.class)
public class UserStateServiceTest {

	private static final RetryTemplate OPTIMISTIC_LOCK_RETRY_TEMPLATE = RetryTemplate.builder()
			.retryOn(OptimisticLockingFailureException.class)
			.maxAttempts(3)
			.fixedBackoff(200)
			.build();
	private static final Map<ProgressionType, Long> UPDATED_PROGRESSION = Map.of(SOURCE_1_TOTAL, 7L);
	private static final Function<UserState, UserState> USER_STATE_UPDATE_FUNCTION =
			userState -> userState.toBuilder().progressions(UPDATED_PROGRESSION).build();
	private static final Long UPDATE_TIMESTAMP = 1010L;
	private static final UserConfiguration UPDATED_USER_CONFIGURATION = UserConfiguration.builder()
			.id(CONFIGURATION_ID)
			.updateTimestamp(UPDATE_TIMESTAMP)
			.progressionsConfiguration(PROGRESSIONS_CONFIGURATION_2)
			.build();
	private static final UserState UPDATED_USER_STATE = USER_STATE.toBuilder().progressions(UPDATED_PROGRESSION).build();

	@Mock
	private UserStatePersistenceService userStatePersistenceService;
	@Mock
	private ConfigurationService configurationService;
	@Mock
	private SegmentationService segmentationService;
	@Mock
	private Clock clock;
	private UserStateService userStateService;

	@BeforeEach
	void setUp() {
		userStateService = new UserStateService(
				OPTIMISTIC_LOCK_RETRY_TEMPLATE, userStatePersistenceService,
				configurationService, segmentationService, clock);
	}

	@Test
	void shouldFindActiveUserSate() {
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);

		var userState = userStateService.findActiveUserState(USER_ID);

		assertThat(userState).isEqualTo(USER_STATE);
	}

	@Test
	void shouldThrowException_ifUserStateIsNotFound() {
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userStateService.findActiveUserState(USER_ID))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Active user state is not found by id=" + USER_ID);
	}

	@Test
	void shouldThrowException_ifUserStateIsNotActive() {
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(null);

		assertThatThrownBy(() -> userStateService.findActiveUserState(USER_ID))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Active user state is not found by id=" + USER_ID);
	}

	@Test
	void shouldUpdateUserState_withLatestConfigurationUpdatesSync() {
		// given
		var expectedUpdatedUserState = USER_STATE.toBuilder()
				.configuration(UPDATED_USER_CONFIGURATION)
				.progressions(UPDATED_PROGRESSION)
				.build();

		// and
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(true);
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(SEGMENT_2);
		when(clock.millis()).thenReturn(UPDATE_TIMESTAMP);
		when(userStatePersistenceService.save(expectedUpdatedUserState)).thenReturn(expectedUpdatedUserState);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION).get();

		// then
		assertThat(updatedUserState).isEqualTo(expectedUpdatedUserState);

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getCachedActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(segmentationService).evaluate(USER_ID, SEGMENTS);
		verify(clock).millis();
		verify(userStatePersistenceService).save(expectedUpdatedUserState);
	}

	@Test
	void shouldUpdateUserState_withoutLatestConfigurationUpdatesSync() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(false);
		when(userStatePersistenceService.save(UPDATED_USER_STATE)).thenReturn(UPDATED_USER_STATE);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION).get();

		// then
		assertThat(updatedUserState).isEqualTo(UPDATED_USER_STATE);

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getCachedActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(segmentationService, never()).evaluate(any(), any());
		verify(clock, never()).millis();
		verify(userStatePersistenceService).save(UPDATED_USER_STATE);
	}

	@Test
	void shouldNotReturnUpdatedUserState_ifConfigurationNoLongerApplicableForUser() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(true);
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(null);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION);

		// then
		assertThat(updatedUserState.isEmpty()).isTrue();

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getCachedActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(segmentationService).evaluate(USER_ID, SEGMENTS);
		verify(clock, never()).millis();
		verify(userStatePersistenceService, never()).save(any());
	}

	@Test
	void shouldNotReturnUpdatedUserState_ifConfigurationNoLongerExist() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(null);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION);

		// then
		assertThat(updatedUserState.isEmpty()).isTrue();

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getCachedActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService, never()).shouldReevaluateSegmentation(anyLong());
		verify(segmentationService, never()).evaluate(any(), any());
		verify(clock, never()).millis();
		verify(userStatePersistenceService, never()).save(any());
	}

	@Test
	void shouldUpdateUserState_withRetriesForOptimisticLocks() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(false);
		when(userStatePersistenceService.save(UPDATED_USER_STATE))
				.thenThrow(OptimisticLockingFailureException.class)
				.thenThrow(OptimisticLockingFailureException.class)
				.thenReturn(UPDATED_USER_STATE);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION).get();

		// then
		assertThat(updatedUserState).isEqualTo(UPDATED_USER_STATE);

		// and
		verify(userStatePersistenceService, times(3)).find(USER_ID);
		verify(configurationService, times(3)).getCachedActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService, times(3)).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(userStatePersistenceService, times(3)).save(UPDATED_USER_STATE);
	}

	@Test
	void shouldThrowException_ifRetriesForOptimisticLocksAreExhausted() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getCachedActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(false);
		when(userStatePersistenceService.save(UPDATED_USER_STATE))
				.thenThrow(OptimisticLockingFailureException.class)
				.thenThrow(OptimisticLockingFailureException.class)
				.thenThrow(OptimisticLockingFailureException.class);

		// expect
		assertThatThrownBy(() -> userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION))
				.isInstanceOf(OptimisticLockingFailureException.class);

		// and
		verify(userStatePersistenceService, times(3)).find(USER_ID);
		verify(configurationService, times(3)).getCachedActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService, times(3)).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(userStatePersistenceService, times(3)).save(UPDATED_USER_STATE);
	}
}
