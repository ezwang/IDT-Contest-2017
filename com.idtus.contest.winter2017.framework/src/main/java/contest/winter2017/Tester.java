package contest.winter2017;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

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
	static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------------------";

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
		
		/** path to the detailed html output file */
		public String htmlFilePath;

		/** number of threads to use */
		public int numThreads;

		/** disable use of json files */
		public boolean disableJsonConversion;

		/** only output yaml */
		public boolean yamlOnly;

		/** enable verbose output */
		public boolean verbose;
		
		/** how many security tests to run */
		public int securityTestIterations;
		
		/** when to stop security testing */
		public int securityTestTime;
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
	private BasicTester basicTester;
	private SecurityTester securityTester;
	
	private String jarName;

	private boolean optionYamlOnly;
	private boolean optionVerbose;

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
		
		this.jarName = FilenameUtils.getName(options.jarToTestPath);
	}


	public void printYaml() {
		assert basicTester != null;
		assert securityTester != null;

		System.out.println(basicTester.getYaml());
		System.out.println("Total code coverage percentage: " +
				coverage.generateSummaryCodeCoverageResults());
		System.out.println(securityTester.getYaml());
	}


	/**
	 * Execute basic tests, and print information.
	 */
	public void executeBasicTests() {
		basicTester = new BasicTester(programRunner);

		try {
			basicTester.runTests(this.tests);
		}
		catch (Exception e) {
			if (!optionYamlOnly) {
				System.out.println("Error executing basic tests: " + e);
				e.printStackTrace();
			}
			return;
		}

		// print the basic test results and the code coverage associated with the basic tests
		double percentCovered = coverage.generateSummaryCodeCoverageResults();

		if (!optionYamlOnly) {
			basicTester.printInfo(optionVerbose);
			System.out.printf("%.2f percent covered%n", percentCovered);
			System.out.println(HORIZONTAL_LINE);
		}
	}


	/**
	 * Execute security tests, and print information.
	 */
	public void executeSecurityTests() {
		securityTester = new SecurityTester(programRunner);

		try {
			securityTester.runTests(parameterFactory, this.tests);
		}
		catch (Exception e) {
			if (!optionYamlOnly) {
				System.out.println("Error executing security tests: " + e);
				e.printStackTrace();
			}
			return;
		}

		// We do not intend for this example code to be part of your output. We are only
		// including the example to show you how you might tap into the code coverage
		// results that we are generating with jacoco
		if (!optionYamlOnly) {
			securityTester.printInfo(optionVerbose);
		}
	}


	//////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////


	/**
	 * Method used to print the basic test output (std out/err)
	 * @param output - Output object containing std out/err to print 
	 */
	static void printBasicTestOutput(Output output) {
		System.out.println("stdout of execution: " + output.getStdOutString());
		System.out.println("stderr of execution: " + output.getStdErrString());
	}


	public void generateHtmlOutput(String path) {
		EnvironmentConfiguration env = EnvironmentConfigurationBuilder.configuration().functions().add(new JtwigClassFunction()).and().build();
		JtwigTemplate template = JtwigTemplate.classpathTemplate("/output.twig", env);
		JtwigModel model = JtwigModel.newModel();
		model.with("jarName", jarName);
		model.with("coverage", coverage.generateDetailedCodeCoverageResults());
		model.with("summary", coverage.generateSummaryCodeCoverageResults());
		
		model.with("basic", basicTester);
		model.with("security", securityTester);
		
		model.with("timestamp", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
		
		File htmlFile = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(htmlFile);
			template.render(model, out);
			out.close();
			
			if (!optionYamlOnly) {
				System.out.println("Detailed HTML output was generated to " + path + ".");
				System.out.println();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

}
