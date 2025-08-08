package root.application.model.event;

public interface Event {

	EventSource getSource();

	String getUserId();
}
