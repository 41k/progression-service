package root.infrastructure.dto;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;
import root.application.model.ProgressionType;
import root.application.model.UserState;

@Value
public class UserStateResponse {

	String userId;
	Map<ProgressionType, ProgressionDto> progressionState;

	public UserStateResponse(UserState userState) {
		userId = userState.getUserId();
		progressionState = userState.getConfiguration().progressionsConfiguration().entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> ProgressionDto.builder()
						.currentValue(userState.getProgressions().getOrDefault(entry.getKey(), 0L))
						.targetValue(entry.getValue().progressionTarget())
						.reward(RewardDto.fromModel(entry.getValue().reward()))
						.build()
		));
	}
}
