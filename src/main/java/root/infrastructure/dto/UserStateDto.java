package root.infrastructure.dto;

import java.util.Map;

import root.application.model.ProgressionType;

public record UserStateDto(
		String userId,
		Map<ProgressionType, ProgressionStateDto> progressionState
) {
}
