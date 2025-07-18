package root.application.model;

public interface Event {

	EventSource getConsumerType();

	String getUserId();
}
