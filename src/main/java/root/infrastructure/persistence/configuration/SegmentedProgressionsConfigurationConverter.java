package root.infrastructure.persistence.configuration;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import root.application.model.ProgressionConfiguration;
import root.infrastructure.persistence.converter.ZipConverter;

public class SegmentedProgressionsConfigurationConverter
		extends ZipConverter<Map<String, Map<String, ProgressionConfiguration>>> {

	@Override
	protected TypeReference<Map<String, Map<String, ProgressionConfiguration>>> getTypeReference() {
		return new TypeReference<>() {};
	}
}
