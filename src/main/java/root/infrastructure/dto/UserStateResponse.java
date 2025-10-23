package root.infrastructure.dto;

import java.util.Map;

import root.application.model.ProgressionType;

public record UserStateResponse(
		String userId,
		Map<ProgressionType, ProgressionDto> progressionState
) {
}
