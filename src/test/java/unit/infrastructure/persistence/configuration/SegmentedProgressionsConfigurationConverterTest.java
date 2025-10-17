package unit.infrastructure.persistence.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static unit.UnitTestData.SEGMENTED_PROGRESSIONS_CONFIGURATION;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import root.infrastructure.persistence.configuration.SegmentedProgressionsConfigurationConverter;

public class SegmentedProgressionsConfigurationConverterTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final SegmentedProgressionsConfigurationConverter CONVERTER = new SegmentedProgressionsConfigurationConverter();

	@Test
	@SneakyThrows
	void shouldZipAndUnzipValueCorrectly() {
		// given
		var notZippedValueBytes = OBJECT_MAPPER.writeValueAsBytes(SEGMENTED_PROGRESSIONS_CONFIGURATION);

		// when
		var zippedValueBytes = CONVERTER.convertToDatabaseColumn(SEGMENTED_PROGRESSIONS_CONFIGURATION);

		// then
		assertThat(zippedValueBytes.length < notZippedValueBytes.length).isTrue();

		// when
		var unzippedValue = CONVERTER.convertToEntityAttribute(zippedValueBytes);

		// then
		assertThat(unzippedValue).isEqualTo(SEGMENTED_PROGRESSIONS_CONFIGURATION);
	}
}
