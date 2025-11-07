package root.infrastructure;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import root.application.model.Configuration;
import root.application.model.ProgressionConfiguration;
import root.application.model.ProgressionType;
import root.application.model.Reward;
import root.infrastructure.dto.ConfigurationInfoDto;
import root.infrastructure.dto.ConfigurationRequest;
import root.infrastructure.dto.ConfigurationResponse;
import root.infrastructure.dto.PaginatedResponse;
import root.infrastructure.dto.ProgressionConfigurationDto;
import root.infrastructure.dto.RewardDto;
import root.infrastructure.persistence.configuration.ConfigurationEntity;

@Mapper
public interface ConfigurationMapper {

	ConfigurationEntity toEntity(ConfigurationRequest request);

	Configuration toModel(ConfigurationEntity entity);

	ConfigurationResponse toResponse(ConfigurationEntity entity);

	@Mapping(target = "currentPage", source = "page.number")
	@Mapping(target = "pageSize", source = "page.size")
	@Mapping(target = "data", source = "page.content", defaultExpression = "java(List.of())")
	PaginatedResponse<ConfigurationInfoDto> toResponse(Page<ConfigurationEntity> page);

	List<ConfigurationInfoDto> toDto(List<ConfigurationEntity> list);

	ProgressionConfiguration toModel(ProgressionConfigurationDto dto);

	ProgressionConfigurationDto toDto(ProgressionConfiguration model);

	Map<ProgressionType, ProgressionConfiguration> toProgressionConfigurationPerTypeModel(Map<ProgressionType, ProgressionConfigurationDto> dto);

	Map<String, Map<ProgressionType, ProgressionConfiguration>> toSegmentedProgressionsConfigurationModel(Map<String, Map<ProgressionType, ProgressionConfigurationDto>> dto);

	Map<ProgressionType, ProgressionConfigurationDto> toProgressionConfigurationPerTypeDto(Map<ProgressionType, ProgressionConfiguration> model);

	Map<String, Map<ProgressionType, ProgressionConfigurationDto>> toSegmentedProgressionsConfigurationDto(Map<String, Map<ProgressionType, ProgressionConfiguration>> model);

	Reward toModel(RewardDto dto);

	RewardDto toDto(Reward model);
}
