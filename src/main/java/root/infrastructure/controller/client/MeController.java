package root.infrastructure.controller.client;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.service.UserStateService;
import root.infrastructure.ConfigurationMapper;
import root.infrastructure.dto.ProgressionDto;
import root.infrastructure.dto.UserStateResponse;

@Tag(name = "Me API")
@RestController
@RequestMapping(path = "/${spring.application.name}/api/public/v1/me", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class MeController {

	public static final String USER_ID_HEADER = "USER-ID";

	private final UserStateService userStateService;
	private final ConfigurationMapper mapper;

	@GetMapping
	public UserStateResponse getUserState(@RequestHeader(USER_ID_HEADER) String userId) {
		log.debug("Get active state for user with id={}", userId);
		var userState = userStateService.findActiveUserState(userId);
		var progressionState = userState.getConfiguration().progressionsConfiguration().entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> ProgressionDto.builder()
						.currentValue(userState.getProgressions().getOrDefault(entry.getKey(), 0L))
						.targetValue(entry.getValue().progressionTarget())
						.reward(mapper.toDto(entry.getValue().reward()))
						.build()
		));
		return new UserStateResponse(userId, progressionState);
	}
}
