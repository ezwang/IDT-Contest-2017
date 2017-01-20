package contest.winter2017;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TestBoundsParser {
	private static final String FIXED_PARAMETER_KEY = "fixed parameter list";
	private static final String DEPENDENT_PARAMETER_KEY = "dependent parameters";

	// Utility values for JSON parsing
	private static final Type STRING_OBJECT_MAP = new TypeToken<HashMap<String, Object>>(){}.getType();

	@SuppressWarnings("rawtypes")
	private static final JsonSerializer<Class> JSON_SERIALIZER = new JsonSerializer<Class>() {
		@Override
		public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.getName());
		}
	};

	private static Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.registerTypeAdapter(java.lang.Class.class, JSON_SERIALIZER)
			.create();


	// Original map
	private Map<String, Object> originalMap;

	// Parsed values
	private List<Test> tests;
	private ParameterFactory parameterFactory;


	/**
	 * Create a TestBoundsParser from a map.
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	public TestBoundsParser(Map<String, Object> map) {
		originalMap = map;

		// fill in tests
		tests = new ArrayList<Test>();
		List<Map<String, Object>> testList = (List<Map<String, Object>>) map.get("tests");
		for (Map<String, Object> inTest : testList) {
			tests.add(new Test(inTest));
		}

		// fill in parameterFactory
		if (map.containsKey(FIXED_PARAMETER_KEY)) {
			List<Object> paramList = (List<Object>) map.get(FIXED_PARAMETER_KEY);
			parameterFactory = new FixedParameterFactory(paramList);
		}
		else if (map.containsKey(DEPENDENT_PARAMETER_KEY)) {
			Map<String, Object> paramMap = (Map<String, Object>) map.get(DEPENDENT_PARAMETER_KEY);
			parameterFactory = new DependentParameterFactory(paramMap);
		}
	}


	/**
	 * Read test bounds from a JSON file.
	 * @param testFile The JSON file to read from
	 * @return Parsed test bounds
	 * @throws IOException
	 */
	public static TestBoundsParser fromJson(File testFile) throws IOException {
		JsonReader reader = gson.newJsonReader(new FileReader(testFile));
		Map<String, Object> map = gson.fromJson(reader, STRING_OBJECT_MAP);
		return new TestBoundsParser(map);
	}


	/**
	 * Read test bounds from a JAR file.
	 * @param jarFile The JAR file to read from
	 * @return Parsed test bounds
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public static TestBoundsParser fromJar(File jarFile)
			throws IOException, ReflectiveOperationException {

		// figuring out where the entry-point (main class) is in the jar under test
		URL jarURL = new URL("jar:" + jarFile.toURI().toString() + "!/");
		JarURLConnection jarURLconn = (JarURLConnection) jarURL.openConnection();

		Attributes attr = jarURLconn.getMainAttributes();
		String mainClassName = attr.getValue(Attributes.Name.MAIN_CLASS);

		// loading the TestBounds class from the jar under test
		URL fileURL = jarFile.toURI().toURL();
		URLClassLoader cl = URLClassLoader.newInstance(new URL[]{fileURL});

		String mainClassTestBoundsName = mainClassName + "TestBounds";
		Class<?> mainClassTestBounds = cl.loadClass(mainClassTestBoundsName);

		// use reflection to invoke the TestBounds class to get the usage information from the jar
		Method testBoundsMethod = mainClassTestBounds.getMethod("testBounds");
		Object mainClassTestBoundsInstance = mainClassTestBounds.newInstance();

		@SuppressWarnings("unchecked")
		Map<String, Object> mainClassTestBoundsMap =
			(Map<String, Object>) testBoundsMethod.invoke(mainClassTestBoundsInstance);

		return new TestBoundsParser(mainClassTestBoundsMap);
	}

	public Map<String, Object> getOriginalMap() {
		return originalMap;
	}

	public List<Test> getTests() {
		return tests;
	}

	public ParameterFactory getParameterFactory() {
		return parameterFactory;
	}

	/**
	 * Write test bounds to a JSON file.
	 * @param testFile The JSON file to write to
	 * @throws IOException
	 */
	public void writeJson(File testFile) throws IOException {
		JsonWriter writer = gson.newJsonWriter(new FileWriter(testFile));
		gson.toJson(this.originalMap, STRING_OBJECT_MAP, writer);
		writer.close();
	}
}
