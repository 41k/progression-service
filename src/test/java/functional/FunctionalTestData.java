package functional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FunctionalTestData {

	public static final String CREATE_CONFIGURATION_REQUEST = getFileContent("http/request/create-configuration-request.json");

	@SneakyThrows
	private static String getFileContent(String filePath) {
		var path = Paths.get(FunctionalTestData.class.getClassLoader().getResource(filePath).toURI());
		try (var lines = Files.lines(path)) {
			return lines.collect(Collectors.joining("\n"));
		} catch (Exception e) {
			throw new IOException("Failed to load file: %s".formatted(filePath), e);
		}
	}
}
