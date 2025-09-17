package root.infrastructure.controller.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.application.service.UserStateService;
import root.infrastructure.dto.UserStateResponse;

@RestController
@RequestMapping(path = "/${spring.application.name}/public/v1/me", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class MeController {

	public static final String USER_ID_HEADER = "USER-ID";

	private final UserStateService userStateService;

	@GetMapping
	public UserStateResponse getUserState(@RequestHeader(USER_ID_HEADER) String userId) {
		log.debug("Get active state for user with id={}", userId);
		return new UserStateResponse(userStateService.findActiveUserState(userId));
	}
}
