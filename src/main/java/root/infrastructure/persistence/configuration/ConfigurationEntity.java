package root.infrastructure.persistence.configuration;

import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.application.model.ProgressionConfiguration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "progressions_configurations")
public class ConfigurationEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotNull
	private Long startTimestamp;
	@NotNull
	private Long endTimestamp;
	@NotNull
	private Long updateTimestamp;
	@Valid
	@Convert(converter = SegmentedProgressionsConfigurationConverter.class)
	@Column(columnDefinition = "MEDIUMBLOB")
	private Map<String, Map<String, ProgressionConfiguration>> segmentedProgressionsConfiguration;
}
