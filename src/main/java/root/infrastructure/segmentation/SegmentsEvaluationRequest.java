package root.infrastructure.segmentation;

import java.util.Set;

public record SegmentsEvaluationRequest(
		String userId,
		Set<String> segments
) {
}
