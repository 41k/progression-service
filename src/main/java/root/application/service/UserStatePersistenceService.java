package root.application.service;

import java.util.Optional;

import root.application.model.UserState;

public interface UserStatePersistenceService {

	Optional<UserState> find(String userId);

	UserState save(UserState userState);
}
