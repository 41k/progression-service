package root.infrastructure.segmentation;

import java.time.Clock;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import root.application.service.SegmentationService;
import root.configuration.properties.SegmentationProperties;

@Service
@RequiredArgsConstructor
public class SegmentEvaluationService implements SegmentationService {

	private final Clock clock;
	private final SegmentationProperties properties;
	private final RestTemplate segmentationRestTemplate;

	@Override
	public boolean shouldReevaluateSegmentation(long lastEvaluationTimestamp) {
		return clock.millis() - lastEvaluationTimestamp > properties.reevaluationPeriodMillis();
	}

	@Override
	public String evaluate(String userId, Set<String> segments) {
		var request = buildRequest(userId, segments);
		var response = segmentationRestTemplate.exchange(
				properties.url(), HttpMethod.POST, request, SegmentsEvaluationResponse.class).getBody();
		return response.segment();
	}

	private HttpEntity<SegmentsEvaluationRequest> buildRequest(String userId, Set<String> segments) {
		var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		var body = new SegmentsEvaluationRequest(userId, segments);
		return new HttpEntity<>(body, headers);
	}
}
