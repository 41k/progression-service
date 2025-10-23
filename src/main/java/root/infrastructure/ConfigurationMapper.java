package root.infrastructure;

import java.time.Clock;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import root.application.model.Configuration;
import root.application.model.ProgressionConfiguration;
import root.application.model.ProgressionType;
import root.application.model.Reward;
import root.infrastructure.dto.ConfigurationInfoDto;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.ProgressionConfigurationDto;
import root.infrastructure.dto.RewardDto;
import root.infrastructure.persistence.configuration.ConfigurationEntity;

// todo: check if all methods are used
// todo: find proper names for methods
@Mapper
public abstract class ConfigurationMapper {

	@Autowired
	protected Clock clock;

	@Mapping(target = "updateTimestamp", expression = "java(clock.millis())")
	public abstract ConfigurationEntity toEntity(ConfigurationRequest request);

	public abstract Configuration toModel(ConfigurationEntity entity);

	public abstract ConfigurationResponse toDto(ConfigurationEntity entity);

	public abstract ConfigurationInfoDto toInfoDto(ConfigurationEntity entity);

	public abstract ProgressionConfiguration toModel(ProgressionConfigurationDto dto);

	public abstract ProgressionConfigurationDto toDto(ProgressionConfiguration model);

	// todo: try to get rid of map declaration leave ProgressionConfiguration only
	public abstract Map<ProgressionType, ProgressionConfiguration> toModel1(Map<ProgressionType, ProgressionConfigurationDto> dto);

	// todo: try to get rid of map declaration leave ProgressionConfigurationDto only
	public abstract Map<ProgressionType, ProgressionConfigurationDto> toDto1(Map<ProgressionType, ProgressionConfiguration> model);

	// todo: try to get rid of map declaration leave ProgressionConfiguration only
	public abstract Map<String, Map<ProgressionType, ProgressionConfiguration>> toModel(Map<String, Map<ProgressionType, ProgressionConfigurationDto>> dto);

	// todo: try to get rid of map declaration leave ProgressionConfigurationDto only
	public abstract Map<String, Map<ProgressionType, ProgressionConfigurationDto>> toDto(Map<String, Map<ProgressionType, ProgressionConfiguration>> model);

	public abstract Reward toModel(RewardDto dto);

	public abstract RewardDto toDto(Reward model);
}
