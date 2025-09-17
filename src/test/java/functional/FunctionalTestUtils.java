package functional;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FunctionalTestUtils {

	// todo: try to substitute by [net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson]
	// assertThatJson(actual)
	//				.when(Option.IGNORING_EXTRA_FIELDS)
	//				.isEqualTo(expected);
	public static boolean jsonMatch(String actual, String expected) {
		try {
			return JSONCompare.compareJSON(expected, actual, JSONCompareMode.NON_EXTENSIBLE).passed();
		} catch (JSONException e) {
			return false;
		}
	}
}
