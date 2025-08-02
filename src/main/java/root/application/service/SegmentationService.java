package root.application.service;

import java.util.Set;

public interface SegmentationService {

	boolean shouldReevaluateSegmentation(long lastSegmentationTimestamp);

	String evaluate(String userId, Set<String> segments);
}
