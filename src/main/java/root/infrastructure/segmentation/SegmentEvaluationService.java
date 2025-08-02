package root.infrastructure.segmentation;

import java.time.Clock;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import root.application.service.SegmentationService;

@Service
@RequiredArgsConstructor
public class SegmentEvaluationService implements SegmentationService {


	private final Clock clock;
	@Value("${segmentation.refresh-period-millis:300000}") // todo: introduce segmentation properties
	private final long segmentationRefreshPeriodInMillis;
	@Value("${segmentation.url}")
	private final String url;
	private final RestTemplate segmentationRestTemplate;

	@Override
	public boolean shouldReevaluateSegmentation(long lastSegmentationTimestamp) {
		return clock.millis() - lastSegmentationTimestamp > segmentationRefreshPeriodInMillis;
	}

	@Override
	public String evaluate(String userId, Set<String> segments) {
		var request = buildRequest(userId, segments);
		var response = segmentationRestTemplate.exchange(url, HttpMethod.POST, request, SegmentsEvaluationResponse.class).getBody();
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
