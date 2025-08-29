package root.infrastructure.controller.client;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.model.UserState;
import root.application.service.UserStateService;
import root.infrastructure.dto.ProgressionStateDto;
import root.infrastructure.dto.UserStateDto;

// todo: functional tests
@RestController
@RequestMapping(path = "/${spring.application.name}/public/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class UserController {

	private final UserStateService userStateService;

	@GetMapping("/{id}")
	public UserStateDto getUserState(@PathVariable String id) {
		log.debug("Get active state for user with id={}", id);
		return toDto(userStateService.findActiveUserState(id));
	}

	private UserStateDto toDto(UserState userState) {
		var progressionsConfiguration = userState.getConfiguration().progressionsConfiguration();
		var progressions = userState.getProgressions();
		var progressionState = progressionsConfiguration.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> ProgressionStateDto.builder()
						.currentValue(progressions.getOrDefault(entry.getKey(), 0L))
						.targetValue(entry.getValue().progressionTarget())
						.reward(entry.getValue().reward())
						.build()
		));
		return new UserStateDto(userState.getUserId(), progressionState);
	}
}
