package root.infrastructure.segmentation;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import root.application.service.SegmentationService;

@Service
public class SegmentEvaluationService implements SegmentationService {

	private final String url;
	private final RestTemplate restTemplate;

	public SegmentEvaluationService(
			@Value("${segmentation.url}") String url,
			RestTemplate segmentationRestTemplate
	) {
		this.url = url;
		this.restTemplate = segmentationRestTemplate;
	}

	@Override
	public String evaluate(String userId, Set<String> segments) {
		var request = buildRequest(userId, segments);
		var response = restTemplate.exchange(url, HttpMethod.POST, request, SegmentsEvaluationResponse.class).getBody();
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
