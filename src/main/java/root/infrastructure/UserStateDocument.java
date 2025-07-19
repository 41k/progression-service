package root.infrastructure;

import java.util.Map;

import org.springframework.data.aerospike.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.application.model.UserProgressionsConfiguration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "${user-state.document}")
public class UserStateDocument {
	@Id
	private String id;
	private UserProgressionsConfiguration progressionConfiguration;
	private Map<String, Long> progressions;
	@Version
	private Long version;
}
