package unit.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static root.application.model.ProgressionType.SOURCE_1_TOTAL;
import static unit.UnitTestData.CONFIGURATION;
import static unit.UnitTestData.CONFIGURATION_ID;
import static unit.UnitTestData.CONFIGURATION_UPDATE_TIMESTAMP;
import static unit.UnitTestData.PROGRESSIONS_CONFIGURATION_2;
import static unit.UnitTestData.SEGMENTS;
import static unit.UnitTestData.SEGMENT_1;
import static unit.UnitTestData.SEGMENT_2;
import static unit.UnitTestData.USER_CONFIGURATION;
import static unit.UnitTestData.USER_ID;
import static unit.UnitTestData.USER_PROGRESSIONS;
import static unit.UnitTestData.USER_STATE;
import static unit.UnitTestData.USER_STATE_VERSION;

import java.time.Clock;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

	@Spy
	private RetryTemplate optimisticLockRetryTemplate = RetryTemplate.builder()
			.retryOn(OptimisticLockingFailureException.class)
			.maxAttempts(3)
			.fixedBackoff(200)
			.build();
	@Mock
	private UserStatePersistenceService userStatePersistenceService;
	@Mock
	private ConfigurationService configurationService;
	@Mock
	private SegmentationService segmentationService;
	@Mock
	private Clock clock;
	@InjectMocks
	private UserStateService userStateService;

	@Test
	void assignActivateConfigurationToUser_withRetriesForOptimisticLocks() {
		// given
		var userState = UserState.builder()
				.userId(USER_ID)
				.configuration(UserConfiguration.builder().id(5).build())
				.progressions(USER_PROGRESSIONS)
				.version(USER_STATE_VERSION)
				.build();
		var userStateWithNewConfiguration = UserState.builder()
				.userId(USER_ID)
				.configuration(USER_CONFIGURATION)
				.version(USER_STATE_VERSION)
				.build();

		// and
		when(configurationService.getActiveConfiguration()).thenReturn(CONFIGURATION);
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(userState));
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(SEGMENT_1);
		when(clock.millis()).thenReturn(CONFIGURATION_UPDATE_TIMESTAMP);
		when(userStatePersistenceService.save(userStateWithNewConfiguration))
				.thenThrow(OptimisticLockingFailureException.class)
				.thenThrow(OptimisticLockingFailureException.class)
				.thenReturn(userStateWithNewConfiguration);

		// when
		userStateService.assignActivateConfigurationToUser(USER_ID);

		// then
		verify(configurationService, times(3)).getActiveConfiguration();
		verify(userStatePersistenceService, times(3)).find(USER_ID);
		verify(segmentationService, times(3)).evaluate(USER_ID, SEGMENTS);
		verify(clock, times(3)).millis();
		verify(userStatePersistenceService, times(3)).save(userStateWithNewConfiguration);
	}

	@Test
	void assignActivateConfigurationToUser_shouldCreateUserStateWithActiveConfiguration_ifUserStateIsNotFound() {
		// given
		when(configurationService.getActiveConfiguration()).thenReturn(CONFIGURATION);
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.empty());
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(SEGMENT_1);
		when(clock.millis()).thenReturn(CONFIGURATION_UPDATE_TIMESTAMP);

		// and
		var userState = UserState.builder().userId(USER_ID).configuration(USER_CONFIGURATION).build();

		// when
		userStateService.assignActivateConfigurationToUser(USER_ID);

		// then
		verify(configurationService).getActiveConfiguration();
		verify(userStatePersistenceService).find(USER_ID);
		verify(segmentationService).evaluate(USER_ID, SEGMENTS);
		verify(clock).millis();
		verify(userStatePersistenceService).save(userState);
	}

	@Test
	void assignActivateConfigurationToUser_shouldNotDoNothing_ifThereIsNoActiveConfiguration() {
		// given
		when(configurationService.getActiveConfiguration()).thenReturn(null);

		// when
		userStateService.assignActivateConfigurationToUser(USER_ID);

		// then
		verify(configurationService).getActiveConfiguration();
		verifyNoMoreInteractions(configurationService, userStatePersistenceService, segmentationService, clock);
	}

	@Test
	void assignActivateConfigurationToUser_shouldNotDoNothing_ifUserAlreadyHasThisConfiguration() {
		// given
		when(configurationService.getActiveConfiguration()).thenReturn(CONFIGURATION);
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));

		// when
		userStateService.assignActivateConfigurationToUser(USER_ID);

		// then
		verify(configurationService).getActiveConfiguration();
		verify(userStatePersistenceService).find(USER_ID);
		verifyNoMoreInteractions(configurationService, userStatePersistenceService, segmentationService, clock);
	}

	@Test
	void assignActivateConfigurationToUser_shouldNotDoNothing_ifConfigurationIsNotApplicableForUser() {
		// given
		var userState = UserState.builder()
				.userId(USER_ID)
				.configuration(UserConfiguration.builder().id(3).build())
				.build();

		// and
		when(configurationService.getActiveConfiguration()).thenReturn(CONFIGURATION);
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(userState));
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(null);

		// when
		userStateService.assignActivateConfigurationToUser(USER_ID);

		// then
		verify(configurationService).getActiveConfiguration();
		verify(userStatePersistenceService).find(USER_ID);
		verify(segmentationService).evaluate(USER_ID, SEGMENTS);
		verifyNoMoreInteractions(clock, userStatePersistenceService);
	}

	@Test
	void findActiveUserState() {
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);

		var userState = userStateService.findActiveUserState(USER_ID);

		assertThat(userState).isEqualTo(USER_STATE);
	}

	@Test
	void findActiveUserState_shouldThrowException_ifUserStateIsNotFound() {
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userStateService.findActiveUserState(USER_ID))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Active user state is not found by id=" + USER_ID);
	}

	@Test
	void findActiveUserState_shouldThrowException_ifUserStateIsNotActive() {
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(null);

		assertThatThrownBy(() -> userStateService.findActiveUserState(USER_ID))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("Active user state is not found by id=" + USER_ID);
	}

	@Test
	void updateUserStateIfPresent_shouldUpdateUserState_withLatestConfigurationUpdatesSync() {
		// given
		var expectedUpdatedUserState = USER_STATE.toBuilder()
				.configuration(UPDATED_USER_CONFIGURATION)
				.progressions(UPDATED_PROGRESSION)
				.build();

		// and
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(true);
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(SEGMENT_2);
		when(clock.millis()).thenReturn(UPDATE_TIMESTAMP);
		when(userStatePersistenceService.save(expectedUpdatedUserState)).thenReturn(expectedUpdatedUserState);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION).orElseThrow();

		// then
		assertThat(updatedUserState).isEqualTo(expectedUpdatedUserState);

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(segmentationService).evaluate(USER_ID, SEGMENTS);
		verify(clock).millis();
		verify(userStatePersistenceService).save(expectedUpdatedUserState);
	}

	@Test
	void updateUserStateIfPresent_shouldUpdateUserState_withoutLatestConfigurationUpdatesSync() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(false);
		when(userStatePersistenceService.save(UPDATED_USER_STATE)).thenReturn(UPDATED_USER_STATE);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION).orElseThrow();

		// then
		assertThat(updatedUserState).isEqualTo(UPDATED_USER_STATE);

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(segmentationService, never()).evaluate(any(), any());
		verify(clock, never()).millis();
		verify(userStatePersistenceService).save(UPDATED_USER_STATE);
	}

	@Test
	void updateUserStateIfPresent_shouldNotReturnUpdatedUserState_ifConfigurationNoLongerApplicableForUser() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(true);
		when(segmentationService.evaluate(USER_ID, SEGMENTS)).thenReturn(null);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION);

		// then
		assertThat(updatedUserState.isEmpty()).isTrue();

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(segmentationService).evaluate(USER_ID, SEGMENTS);
		verify(clock, never()).millis();
		verify(userStatePersistenceService, never()).save(any());
	}

	@Test
	void updateUserStateIfPresent_shouldNotReturnUpdatedUserState_ifConfigurationNoLongerExist() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(null);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION);

		// then
		assertThat(updatedUserState.isEmpty()).isTrue();

		// and
		verify(userStatePersistenceService).find(USER_ID);
		verify(configurationService).getActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService, never()).shouldReevaluateSegmentation(anyLong());
		verify(segmentationService, never()).evaluate(any(), any());
		verify(clock, never()).millis();
		verify(userStatePersistenceService, never()).save(any());
	}

	@Test
	void updateUserStateIfPresent_shouldUpdateUserState_withRetriesForOptimisticLocks() {
		// given
		when(userStatePersistenceService.find(USER_ID)).thenReturn(Optional.of(USER_STATE));
		when(configurationService.getActiveConfigurationById(CONFIGURATION_ID)).thenReturn(CONFIGURATION);
		when(segmentationService.shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP)).thenReturn(false);
		when(userStatePersistenceService.save(UPDATED_USER_STATE))
				.thenThrow(OptimisticLockingFailureException.class)
				.thenThrow(OptimisticLockingFailureException.class)
				.thenReturn(UPDATED_USER_STATE);

		// when
		var updatedUserState = userStateService.updateUserStateIfPresent(USER_ID, USER_STATE_UPDATE_FUNCTION).orElseThrow();

		// then
		assertThat(updatedUserState).isEqualTo(UPDATED_USER_STATE);

		// and
		verify(userStatePersistenceService, times(3)).find(USER_ID);
		verify(configurationService, times(3)).getActiveConfigurationById(CONFIGURATION_ID);
		verify(segmentationService, times(3)).shouldReevaluateSegmentation(CONFIGURATION_UPDATE_TIMESTAMP);
		verify(userStatePersistenceService, times(3)).save(UPDATED_USER_STATE);
	}
}
