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
public class Source2Event implements Event {

	private String userId;
	private long amount;

	@Override
	public EventSource getSource() {
		return EventSource.SOURCE_2;
	}
}
