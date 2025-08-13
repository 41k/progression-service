package root.application.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Source1Event implements Event {

	private Long userId;
	private String result;

	@Override
	public EventSource getSource() {
		return EventSource.SOURCE_1;
	}

	@Override
	public String getUserId() {
		return userId == null ? null : String.valueOf(userId);
	}
}
