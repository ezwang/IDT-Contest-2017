package contest.winter2017;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonParseException;

/**
 * Class that will handle execution of basic tests and exploratory security test on a black-box executable jar.  
 * 
 * Example code that we used to guide our use of Jacoco code coverage was found @ http://www.eclemma.org/jacoco/trunk/doc/api.html
 * 
 * @author IDT
 */
public class Tester {

	/**
	 * horizontal line shown between test output
	 */
	private static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------------------";

	/**
	 * A class to store options for testing.
	 * Used to aid in parsing options from the command line.
	 */
	public static class TesterOptions {
		/** path of the jar to test */
		public String jarToTestPath;

		/** path of the jacoco agent jar */
		public String jacocoAgentJarPath;

		/** path of the directory that jacoco will use for output */
		public String jacocoOutputDirPath;

		/** path of the jacoco output file */
		public String jacocoOutputFilePath;

		/** path to the json file to load or save test bounds */
		public String jsonFilePath;

		/** number of threads to use */
		public int numThreads;

		/** disable use of json files */
		public boolean disableJsonConversion;

		/** only output yaml */
		public boolean yamlOnly;

		/** enable verbose output */
		public boolean verbose;
	}

	/**
	 * basic tests that have been extracted from the jar under test
	 */
	private List<Test> tests = null;

	/**
	 * parameter factory that can be used to help figure out parameter signatures from the blackbox jars
	 */
	private ParameterFactory parameterFactory = null;

	private ProgramRunner programRunner;
	private JacocoCoverageAnalyzer coverage;

	private boolean optionYamlOnly;
	private boolean optionVerbose;

	private int yaml_test_pass = 0;
	private int yaml_test_fail = 0;
	private HashSet<String> yaml_errors = new HashSet<String>();

	//////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////

	/**
	 * Method that will initialize the Framework by loading up the jar to test, and then extracting
	 * parameters, parameter bounds (if any), and basic tests from the jar.
	 */
	public void init(TesterOptions options)
			throws IOException, ReflectiveOperationException, JsonParseException {

		this.optionYamlOnly = options.yamlOnly;
		this.optionVerbose = options.verbose;

		this.programRunner = new ProgramRunner(options);
		this.coverage = new JacocoCoverageAnalyzer(options);

		// parse TestBounds file
		TestBoundsParser testBoundsParser;
		File testFile = new File(options.jsonFilePath);
		if (!options.disableJsonConversion && testFile.exists()) {
			// test cases are already converted to json, load them
			testBoundsParser = TestBoundsParser.fromJson(testFile);
		}
		else {
			// instantiating a new Parameter Factory using the Test Bounds map
			File jarFileToTest = new File(options.jarToTestPath);
			testBoundsParser = TestBoundsParser.fromJar(jarFileToTest);
			if (!options.disableJsonConversion) {
				testBoundsParser.writeJson(testFile);
			}
		}

		this.parameterFactory = testBoundsParser.getParameterFactory();
		this.tests = testBoundsParser.getTests();
	}


	public void printYaml() {
		System.out.println("Total predefined tests run: " + (this.yaml_test_pass + this.yaml_test_fail));
		System.out.println("Number of predefined tests that passed: " + this.yaml_test_pass);
		System.out.println("Number of predefined tests that failed: " + this.yaml_test_fail);
		System.out.println("Total code coverage percentage: " + coverage.generateSummaryCodeCoverageResults());
		System.out.println("Unique error count: " + this.yaml_errors.size());
		if (this.yaml_errors.size() > 0) {
			System.out.println("Errors seen:");
			for (String s : this.yaml_errors) {
				if (s.trim().contains("\n")) {
					System.out.println("  - |-");
					for (String line : s.trim().split("\n")) {
						System.out.println("  - " + line);
					}
				}
				else {
					System.out.println("  - " + s.trim());
				}
			}
		}
		else {
			System.out.println("Errors seen: []");
		}
	}

	/**
	 * This is the half of the framework that IDT has completed. We are able to pull basic tests 
	 * directly from the executable jar. We are able to run the tests and assess the output as PASS/FAIL.
	 * 
	 * You likely do not have to change this part of the framework. We are considering this complete and 
	 * want your team to focus more on the SecurityTests. 
	 * 
	 *  @param threads - the number of threads to use for basic tests
	 */
	public void executeBasicTests(int threads) {
		int passCount = 0;
		int failCount = 0;

		ExecutorService executor = Executors.newFixedThreadPool(threads);
		List<Future<BasicTestResult>> results = new LinkedList<Future<BasicTestResult>>();

		// iterate through the lists of tests and execute each one
		for (Test test : this.tests) {
			results.add(executor.submit(new BasicTestRunner(programRunner, test)));
		}

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Future<BasicTestResult> future : results) {
			BasicTestResult result;
			try {
				result = future.get();
			} catch (InterruptedException | ExecutionException e) {
				failCount++;
				continue;
			}

			printBasicTestOutput(result.output);

			if (result.passed) {
				passCount++;
			}
			else {
				failCount++;
				if (!optionYamlOnly) {
					System.out.println("Test Failed!");
					System.out.println("\t -> parameters: " + result.parameters);
					System.out.println(result.error);
					System.out.println(HORIZONTAL_LINE);
				}
			}
		}

		// print the basic test results and the code coverage associated with the basic tests
		double percentCovered = coverage.generateSummaryCodeCoverageResults();

		if (!optionYamlOnly) {
			System.out.printf("basic test results: %d total, %d pass, %d fail, %.2f percent covered%n",
					passCount + failCount, passCount, failCount, percentCovered);
			System.out.println(HORIZONTAL_LINE);
		}

		this.yaml_test_pass = passCount;
		this.yaml_test_fail = failCount;
	}


	/**
	 * This is the half of the framework that IDT has not completed. We want you to implement your exploratory 
	 * security vulnerability testing here.
	 * 
	 * In an effort to demonstrate some of the features of the framework that you can already utilize, we have
	 * provided some example code in the method. The examples only demonstrate how to use existing functionality. 
	 */
	@SuppressWarnings("rawtypes")
	public void executeSecurityTests() {

		/////////// START EXAMPLE CODE /////////////

		// This example demonstrates how to use the ParameterFactory to figure out the parameter types of parameters
		// for each of the jars under test - this can be a difficult task because of the concepts of fixed and
		// dependent parameters (see the explanation at the top of the ParameterFactory class). As we figure out 
		// what each parameter type is, we are assigning it a simple (dumb) value so that we can use those parameters 
		// to execute the black-box jar. By the time we finish this example, we will have an array of concrete 
		// parameters that we can use to execute the black-box jar.
		List<String> previousParameterStrings = new ArrayList<String>(); // start with a blank parameter list since we are going to start with the first parameter
		List<Parameter> potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
		Parameter potentialParameter;
		while (!potentialParameters.isEmpty()) {
			String parameterString = "";
			potentialParameter = potentialParameters.get(0); 

			//if(potentialParameter.isOptional())  //TODO? - your team might want to look at this flag and handle it as well!

			// an enumeration parameter is one that has multiple options
			if (potentialParameter.isEnumeration()) {
				parameterString = potentialParameter.getEnumerationValues().get(0) + " "; // dumb logic - given a list of options, always use the first one

				// if the parameter has internal format (eg. "<number>:<number>PM EST")
				if(potentialParameter.isFormatted()) {

					// loop over the areas of the format that must be replaced and choose values
					List<Object> formatVariableValues = new ArrayList<Object>();
					for(Class type :potentialParameter.getFormatVariables(parameterString)) {
						if (type == Integer.class){ 
							formatVariableValues.add(new Integer(1)); // dumb logic - always use '1' for an Integer
						} else if (type == String.class) {
							formatVariableValues.add(new String("one")); // dumb logic - always use 'one' for a String
						}
					}

					//build the formatted parameter string with the chosen values (eg. 1:1PM EST)
					parameterString =
							potentialParameter.getFormattedParameter(
									parameterString, formatVariableValues);
				}
				previousParameterStrings.add(parameterString);
				// if it is not an enumeration parameter, it is either an Integer, Double, or String
			} else {
				if (potentialParameter.getType() == Integer.class){ 
					parameterString = Integer.toString(1) + " ";	// dumb logic - always use '1' for an Integer
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == Double.class) {
					parameterString = Double.toString(1.0) + " ";	// dumb logic - always use '1.0' for a Double
					previousParameterStrings.add(parameterString);
				} else if (potentialParameter.getType() == String.class) {

					// if the parameter has internal format (eg. "<number>:<number>PM EST")
					if(potentialParameter.isFormatted()) {

						// loop over the areas of the format that must be replaced and choose values
						List<Object> formatVariableValues = new ArrayList<Object>();
						for(Class type : potentialParameter.getFormatVariables()) {
							if (type == Integer.class){ 
								formatVariableValues.add(new Integer(1)); // dumb logic - always use '1' for an Integer
							} else if (type == String.class) {
								formatVariableValues.add(new String("one")); // dumb logic - always use 'one' for a String
							}
						}

						//build the formatted parameter string with the chosen values (eg. 1:1PM EST)
						parameterString =
								potentialParameter.getFormattedParameter(formatVariableValues);
					}
					else {
						parameterString = "one ";		// dumb logic - always use 'one' for a String
					}

					previousParameterStrings.add(parameterString);
				} else {
					parameterString = "unknown type";
				}
			}
			// because of the challenge associated with dependent parameters, we must go one parameter
			// at a time, building up the parameter list - getNext is the method that we are using 
			// to get the next set of options, given an accumulating parameter list. 
			potentialParameters = this.parameterFactory.getNext(previousParameterStrings);
		}
		Object[] parameters = previousParameterStrings.toArray();

		// This example demonstrates how to execute the black-box jar with concrete parameters
		// and how to access (print to screen) the standard output and error from the run
		Output output = programRunner.instrumentAndExecuteCode(parameters);
		printBasicTestOutput(output); 

		if (!output.getStdErrString().isEmpty()) {
			String err = output.getStdErrString().trim();
			this.yaml_errors.add(err);
		}

		// We do not intend for this example code to be part of your output. We are only
		// including the example to show you how you might tap into the code coverage
		// results that we are generating with jacoco
		if (!optionYamlOnly) {
			coverage.showCodeCoverageResultsExample();
		}

		/////////// END EXAMPLE CODE ////////////// 

	}


	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////


	/**
	 * Method used to print the basic test output (std out/err)
	 * @param output - Output object containing std out/err to print 
	 */
	void printBasicTestOutput(Output output) {
		if (!optionYamlOnly && optionVerbose) {
			System.out.println("stdout of execution: " + output.getStdOutString());
			System.out.println("stderr of execution: " + output.getStdErrString());
		}
	}

}
