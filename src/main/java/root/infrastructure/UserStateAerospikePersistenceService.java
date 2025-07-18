package root.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Service;

import root.application.model.UserState;
import root.application.service.UserStatePersistenceService;

@Service
public class UserStateAerospikePersistenceService implements UserStatePersistenceService {

	@Override
	public Optional<UserState> find(String userId) {
		return Optional.empty(); // todo
	}

	@Override
	public UserState save(UserState userState) {
		return null; // todo
	}
}
