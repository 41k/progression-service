package unit.infrastructure.segmentation;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static unit.UnitTestData.SEGMENTS;
import static unit.UnitTestData.SEGMENT_1;
import static unit.UnitTestData.USER_ID;

import java.time.Clock;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import root.configuration.properties.SegmentationProperties;
import root.infrastructure.segmentation.SegmentEvaluationService;
import root.infrastructure.segmentation.SegmentsEvaluationRequest;
import root.infrastructure.segmentation.SegmentsEvaluationResponse;

@ExtendWith(MockitoExtension.class)
public class SegmentEvaluationServiceTest {
	@Mock
	private Clock clock;
	@Mock
	private SegmentationProperties segmentationProperties;
	@Mock
	private RestTemplate segmentationRestTemplate;
	@InjectMocks
	private SegmentEvaluationService segmentEvaluationService;

	@ParameterizedTest
	@MethodSource("segmentationReevaluationCheckParams")
	void shouldReevaluateSegmentation_shouldCheckIfSegmentationReevaluationIsNecessary(Long currentTimestamp,
	                                                                                   Long lastEvaluationTimestamp,
	                                                                                   Integer reevaluationPeriod,
	                                                                                   Boolean expectedResult) {
		// given
		when(clock.millis()).thenReturn(currentTimestamp);
		when(segmentationProperties.reevaluationPeriodMillis()).thenReturn(reevaluationPeriod);

		// when
		var result = segmentEvaluationService.shouldReevaluateSegmentation(lastEvaluationTimestamp);

		// then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	void evaluate() {
		// given
		var url = "segmentation-service-url";
		var requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
		var request = new HttpEntity<>(new SegmentsEvaluationRequest(USER_ID, SEGMENTS), requestHeaders);
		var response = new ResponseEntity<>(new SegmentsEvaluationResponse(SEGMENT_1), HttpStatusCode.valueOf(200));

		// and
		when(segmentationProperties.url()).thenReturn(url);
		when(segmentationRestTemplate.exchange(url, HttpMethod.POST, request, SegmentsEvaluationResponse.class)).thenReturn(response);

		// when
		var segment = segmentEvaluationService.evaluate(USER_ID, SEGMENTS);

		// then
		assertThat(segment).isEqualTo(SEGMENT_1);
	}

	private static Stream<Arguments> segmentationReevaluationCheckParams() {
		return Stream.of(
				Arguments.of(1000L, 900L, 200, FALSE),
				Arguments.of(1100L, 1000L, 100, FALSE),
				Arguments.of(1100L, 1000L, 90, TRUE)
		);
	}
}
