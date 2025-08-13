package root.infrastructure.persistence.state;

import java.util.Map;

import org.springframework.data.aerospike.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.application.model.ProgressionType;
import root.application.model.UserConfiguration;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "${user-state.document}")
public class UserStateDocument {
	@Id
	private String id;
	private UserConfiguration configuration;
	private Map<ProgressionType, Long> progressions;
	@Version
	private Long version;
}
