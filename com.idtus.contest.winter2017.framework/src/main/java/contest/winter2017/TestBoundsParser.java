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
	// keys for parsing TestBounds
	private static final String TESTS_KEY = "tests";
	private static final String FIXED_PARAMETER_KEY = "fixed parameter list";
	private static final String DEPENDENT_PARAMETER_KEY = "dependent parameters";

	// keys for parsing Test
	private static final String TEST_PARAMETERS_KEY = "parameters";
	private static final String TEST_STDOUT_KEY = "stdOutExpectedResultRegex";
	private static final String TEST_STDERR_KEY = "stdErrExpectedResultRegex";

	// keys for parsing Parameter
	private static final String TYPE_KEY = "type";
	private static final String ENUMERATED_VALUES_KEY = "enumerated values";
	private static final String OPTIONAL_KEY = "optional";
	private static final String FORMAT_KEY = "format";
	private static final String MIN_KEY = "min";
	private static final String MAX_KEY = "max";

	// strings for parsing TestBounds class
	private static final String TESTBOUNDS_CLASS_FORMAT = "%sTestBounds";
	private static final String TESTBOUNDS_METHOD_NAME = "testBounds";

	// Utility values for JSON parsing
	private static final Type STRING_OBJECT_MAP = new TypeToken<HashMap<String, Object>>(){}.getType();
	private static final JsonSerializer<Class<?>> JSON_SERIALIZER = new JsonSerializer<Class<?>>() {
		@Override
		public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
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
		List<Map<String, Object>> rawTestList =	(List<Map<String, Object>>) map.get(TESTS_KEY);
		tests = parseRawTestList(rawTestList);

		// fill in parameterFactory
		if (map.containsKey(FIXED_PARAMETER_KEY)) {
			List<Map<String, Object>> rawList =
					(List<Map<String, Object>>) map.get(FIXED_PARAMETER_KEY);
			List<Parameter> paramList = parseRawParamList(rawList);
			parameterFactory = new FixedParameterFactory(paramList);
		}
		else if (map.containsKey(DEPENDENT_PARAMETER_KEY)) {
			Map<String, Object> rawMap =
					(Map<String, Object>) map.get(DEPENDENT_PARAMETER_KEY);
			Map<String, List<Parameter>> paramMap = parseRawParamMap(rawMap);
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

		String mainClassTestBoundsName = String.format(TESTBOUNDS_CLASS_FORMAT, mainClassName);
		Class<?> mainClassTestBounds = cl.loadClass(mainClassTestBoundsName);

		// use reflection to invoke the TestBounds class to get the usage information from the jar
		Method testBoundsMethod = mainClassTestBounds.getMethod(TESTBOUNDS_METHOD_NAME);
		Object mainClassTestBoundsInstance = mainClassTestBounds.newInstance();

		@SuppressWarnings("unchecked")
		Map<String, Object> mainClassTestBoundsMap =
			(Map<String, Object>) testBoundsMethod.invoke(mainClassTestBoundsInstance);

		fixTestBoundsMap(mainClassTestBoundsMap);
		return new TestBoundsParser(mainClassTestBoundsMap);
	}


	// some adjustments to test cases need to be made when reading the map from a JAR
	// this is so that they'll be correctly written to the json file
	@SuppressWarnings("unchecked")
	private static void fixTestBoundsMap(Map<String, Object> map) {
		List<Map<String, Object>> tests = (List<Map<String, Object>>) map.get(TESTS_KEY);
		for (Map<String, Object> test : tests) {
			List<Object> oldParameters = (List<Object>) test.get(TEST_PARAMETERS_KEY);
			List<String> newParameters = new ArrayList<>(oldParameters.size());
			for (Object obj : oldParameters) {
				String param;
				if (obj instanceof String) {
					// if the argument is surrounded by quotes, remove the quotes
					// this is because the quotes were originally needed to escape arguments in the shell
					param = (String) obj;
					if (param.matches("^\".*\"$")) {
						param = param.substring(1, param.length()-1);
					}
				} else {
					// convert all parameter objects to String
					param = String.valueOf(obj);
				}
				newParameters.add(param);
			}
			test.put(TEST_PARAMETERS_KEY, newParameters);
		}
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
		// convert all arguments into strings
		JsonWriter writer = gson.newJsonWriter(new FileWriter(testFile));
		gson.toJson(this.originalMap, STRING_OBJECT_MAP, writer);
		writer.close();
	}


	@SuppressWarnings("unchecked")
	private static List<Test> parseRawTestList(List<Map<String, Object>> rawList) {
		List<Test> testList = new ArrayList<>();
		for (Map<String, Object> inputMap : rawList) {
			List<String> parameters = (List<String>) inputMap.get(TEST_PARAMETERS_KEY);
			String stdOutExpectedResultRegex = (String) inputMap.get(TEST_STDOUT_KEY);
			String stdErrExpectedResultRegex = (String) inputMap.get(TEST_STDERR_KEY);
			testList.add(new Test(parameters, stdOutExpectedResultRegex, stdErrExpectedResultRegex));
		}
		return testList;
	}

	private static List<Parameter> parseRawParamList(List<Map<String, Object>> rawList) {
		List<Parameter> outList = new ArrayList<>();
		for (Map<String, Object> map : rawList) {
			outList.addAll(parseRawParam(map));
		}
		return outList;
	}


	@SuppressWarnings("unchecked")
	private static Map<String, List<Parameter>> parseRawParamMap(Map<String, Object> rawMap) {
		Map<String, List<Parameter>> outMap = new HashMap<>();
		for (String key : rawMap.keySet()) {
			Object obj = rawMap.get(key);
			List<Parameter> parameters = new ArrayList<>();
			if (obj instanceof Map) {
				parameters.addAll(parseRawParam((Map<String, Object>) obj));
			}
			else if (obj instanceof List) {
				for (Map<String, Object> paramMap : (List<Map<String, Object>>) obj) {
					parameters.addAll(parseRawParam(paramMap));
				}
			}
			outMap.put(key, parameters);
		}
		return outMap;
	}


	@SuppressWarnings("unchecked")
	public static List<Parameter> parseRawParam(Map<String, Object> inputMap) {
		boolean optional = inputMap.containsKey(OPTIONAL_KEY) && ((Boolean) inputMap.get(OPTIONAL_KEY));
		Object min = inputMap.get(MIN_KEY);
		Object max = inputMap.get(MAX_KEY);

		// get type key as string
		Object typeObj = inputMap.get(TYPE_KEY);
		String className;
		if (typeObj instanceof Class) {
			className = ((Class<?>) typeObj).getName();
		} else if (typeObj instanceof String) {
			className = (String) typeObj;
		} else {
			throw new IllegalArgumentException();
		}

		ArrayList<Parameter> parameterList = new ArrayList<>();

		// put enumerated values as separate parameters with format strings
		if (inputMap.containsKey(ENUMERATED_VALUES_KEY)) {
			assert className.equals(String.class.getName());
			assert min == null && max == null;
			assert !inputMap.containsKey(FORMAT_KEY);

			List<String> enumValues = (List<String>) inputMap.get(ENUMERATED_VALUES_KEY);
			for (String enumFormat : enumValues) {
				parameterList.add(new Parameter(enumFormat, optional));
			}
		}

		// a parameter with only one format string
		else if (inputMap.containsKey(FORMAT_KEY)) {
			assert className.equals(String.class.getName());
			assert min == null && max == null;

			String format = (String) inputMap.get(FORMAT_KEY);
			parameterList.add(new Parameter(format, optional));
		}

		// no format; use the class name to determine a parameter
		else if (inputMap.containsKey(TYPE_KEY)) {
			ParameterType<?> paramType;
			if (className.equals(Integer.class.getName())) {
				paramType = new ParameterType.IntegerType((Integer) min, (Integer) max);
			} else if (className.equals(Double.class.getName())) {
				paramType = new ParameterType.DoubleType((Double) min, (Double) max);
			} else if (className.equals(String.class.getName())) {
				paramType = new ParameterType.StringType();
			} else {
				throw new IllegalArgumentException("don't understand class: " + className);
			}
			parameterList.add(new Parameter(paramType, optional));
		}

		else {
			throw new IllegalArgumentException("can't determine parameter type");
		}

		return parameterList;
	}
}
