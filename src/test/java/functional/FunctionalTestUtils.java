package functional;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FunctionalTestUtils {

	public static boolean jsonMatch(String actual, String expected) {
		try {
			return JSONCompare.compareJSON(expected, actual, JSONCompareMode.NON_EXTENSIBLE).passed();
		} catch (JSONException e) {
			return false;
		}
	}
}
