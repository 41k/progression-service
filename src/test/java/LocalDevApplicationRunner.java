import org.springframework.boot.SpringApplication;

import root.ApplicationRunner;

public class LocalDevApplicationRunner {
	public static void main(String[] args) {
		var application = new SpringApplication(ApplicationRunner.class);
		System.setProperty("spring.profiles.default", "test");
		application.run(args);
	}
}
