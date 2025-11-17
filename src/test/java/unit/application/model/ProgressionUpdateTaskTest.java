package unit.application.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.EVENT;
import static unit.UnitTestData.REWARD_1;
import static unit.UnitTestData.USER_STATE;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	void apply_shouldUpdateProgressionSuccessfully() {
		// given
		when(progressionHandler1.handle(EVENT, USER_STATE)).thenReturn(Optional.of(REWARD_1));
		when(progressionHandler2.handle(EVENT, USER_STATE)).thenReturn(Optional.empty());

		// when
		progressionUpdateTask.apply(USER_STATE);

		// then
		assertThat(progressionUpdateTask.getOutcome()).isEqualTo(List.of(REWARD_1));
	}

	@Test
	void apply_shouldSwallowExceptionThrownByAnyProgressionHandlerAndProceed() {
		// given
		when(progressionHandler1.handle(EVENT, USER_STATE)).thenThrow(new RuntimeException());
		when(progressionHandler2.handle(EVENT, USER_STATE)).thenReturn(Optional.empty());

		// when
		progressionUpdateTask.apply(USER_STATE);

		// then
		assertThat(progressionUpdateTask.getOutcome()).isEqualTo(List.of());
	}
}
