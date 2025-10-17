package unit.infrastructure.persistence.state;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.USER_ID;
import static unit.UnitTestData.USER_STATE;
import static unit.UnitTestData.USER_STATE_DOCUMENT;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import root.infrastructure.persistence.state.UserStateAerospikePersistenceService;
import root.infrastructure.persistence.state.UserStateRepository;

@ExtendWith(MockitoExtension.class)
public class UserStateAerospikePersistenceServiceTest {
	@Mock
	private UserStateRepository repository;
	@InjectMocks
	private UserStateAerospikePersistenceService persistenceService;

	@Test
	void find_shouldFindUserSateById() {
		when(repository.findById(USER_ID)).thenReturn(Optional.of(USER_STATE_DOCUMENT));

		var userState = persistenceService.find(USER_ID).orElseThrow();

		assertThat(userState).isEqualTo(USER_STATE);
	}

	@Test
	void find_shouldReturnEmptyOptional_ifUserStateIsNotFoundById() {
		when(repository.findById(USER_ID)).thenReturn(Optional.empty());

		var userStateOptional = persistenceService.find(USER_ID);

		assertThat(userStateOptional.isEmpty()).isTrue();
	}

	@Test
	void save() {
		// given
		var userStateVersionAfterSave = 4L;
		var savedUserStateDocument = USER_STATE_DOCUMENT.toBuilder().version(userStateVersionAfterSave).build();
		var expectedSavedUserState = USER_STATE.toBuilder().version(userStateVersionAfterSave).build();

		// and
		when(repository.save(USER_STATE_DOCUMENT)).thenReturn(savedUserStateDocument);

		// when
		var savedUserState = persistenceService.save(USER_STATE);

		// then
		assertThat(savedUserState).isEqualTo(expectedSavedUserState);
	}
}
