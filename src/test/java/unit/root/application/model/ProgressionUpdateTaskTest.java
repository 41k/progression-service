package unit.root.application.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static unit.TestData.EVENT;
import static unit.TestData.USER_STATE;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import root.application.model.ProgressionType;
import root.application.model.ProgressionUpdateTask;
import root.application.service.progression.handler.ProgressionHandler;

@ExtendWith(MockitoExtension.class)
public class ProgressionUpdateTaskTest {

	@Mock
	private ProgressionHandler progressionHandler1;
	@Mock
	private ProgressionHandler progressionHandler2;
	private ProgressionUpdateTask progressionUpdateTask;

	@BeforeEach
	void setUp() {
		progressionUpdateTask = new ProgressionUpdateTask(EVENT, List.of(progressionHandler1, progressionHandler2));
	}

	@Test
	void shouldUpdateProgressionSuccessfully() {
		// given
		var userStateAfterUpdateByProgressionHandler1 = USER_STATE.toBuilder().progressions(Map.of(
				ProgressionType.SOURCE_1_TOTAL, 1L
		)).build();
		var userStateAfterUpdateByProgressionHandler2 = USER_STATE.toBuilder().progressions(Map.of(
				ProgressionType.SOURCE_1_TOTAL, 1L,
				ProgressionType.SOURCE_1_WON, 1L
		)).build();
		when(progressionHandler1.handle(EVENT, USER_STATE)).thenReturn(userStateAfterUpdateByProgressionHandler1);
		when(progressionHandler2.handle(EVENT, userStateAfterUpdateByProgressionHandler1)).thenReturn(userStateAfterUpdateByProgressionHandler2);

		// when
		var updatedUserState = progressionUpdateTask.apply(USER_STATE);

		// then
		assertThat(updatedUserState).isEqualTo(userStateAfterUpdateByProgressionHandler2);
	}

	@Test
	void shouldSwallowExceptionThrownByAnyProgressionHandlerAndProceed() {
		// given
		var userStateAfterUpdateByProgressionHandler2 = USER_STATE.toBuilder().progressions(Map.of(
				ProgressionType.SOURCE_1_WON, 1L
		)).build();
		when(progressionHandler1.handle(EVENT, USER_STATE)).thenThrow(new RuntimeException());
		when(progressionHandler2.handle(EVENT, USER_STATE)).thenReturn(userStateAfterUpdateByProgressionHandler2);

		// when
		var updatedUserState = progressionUpdateTask.apply(USER_STATE);

		// then
		assertThat(updatedUserState).isEqualTo(userStateAfterUpdateByProgressionHandler2);
	}
}
