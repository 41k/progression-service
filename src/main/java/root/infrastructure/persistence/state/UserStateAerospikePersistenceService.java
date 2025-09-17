package root.infrastructure.persistence.state;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import root.application.model.UserState;
import root.application.service.UserStatePersistenceService;

@Service
@RequiredArgsConstructor
public class UserStateAerospikePersistenceService implements UserStatePersistenceService {

	private final UserStateRepository repository;

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
				.configuration(userStateDocument.getConfiguration())
				.progressions(userStateDocument.getProgressions())
				.version(userStateDocument.getVersion())
				.build();
	}

	private UserStateDocument mapToUserStateDocument(UserState userState) {
		return UserStateDocument.builder()
				.id(userState.getUserId())
				.configuration(userState.getConfiguration())
				.progressions(userState.getProgressions())
				.version(userState.getVersion())
				.build();
	}
}
