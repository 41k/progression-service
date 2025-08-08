package root.infrastructure.persistence.configuration;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import root.application.model.ProgressionConfiguration;
import root.application.model.ProgressionType;
import root.infrastructure.persistence.converter.ZipConverter;

public class SegmentedProgressionsConfigurationConverter
		extends ZipConverter<Map<String, Map<ProgressionType, ProgressionConfiguration>>> {

	@Override
	protected TypeReference<Map<String, Map<ProgressionType, ProgressionConfiguration>>> getTypeReference() {
		return new TypeReference<>() {};
	}
}
