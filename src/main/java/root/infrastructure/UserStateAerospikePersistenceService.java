package root.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import root.application.model.UserState;
import root.application.service.UserStatePersistenceService;

@Service
@RequiredArgsConstructor
public class UserStateAerospikePersistenceService implements UserStatePersistenceService {

	private final UserStateDocumentRepository repository;

	@Override
	public Optional<UserState> find(String userId) {
		return repository.findById(userId).map(this::mapToUserState);
	}

	@Override
	public UserState save(UserState userState) {
		return mapToUserState(
				repository.save(
						mapToUserStateDocument(userState)));
	}

	private UserState mapToUserState(UserStateDocument userStateDocument) {
		return UserState.builder()
				.userId(userStateDocument.getId())
				.progressionConfiguration(userStateDocument.getProgressionConfiguration())
				.progressions(userStateDocument.getProgressions())
				.version(userStateDocument.getVersion())
				.build();
	}

	private UserStateDocument mapToUserStateDocument(UserState userState) {
		return UserStateDocument.builder()
				.id(userState.getUserId())
				.progressionConfiguration(userState.getProgressionConfiguration())
				.progressions(userState.getProgressions())
				.version(userState.getVersion())
				.build();
	}
}
