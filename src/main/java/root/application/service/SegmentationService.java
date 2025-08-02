package root.application.service;

import java.util.Set;

public interface SegmentationService {

	String evaluate(String userId, Set<String> segments);
}
