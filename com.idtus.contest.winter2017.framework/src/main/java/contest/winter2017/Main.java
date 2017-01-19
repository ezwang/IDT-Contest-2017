package contest.winter2017;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Entry-point class for the black-box testing framework 
 * 
 * @author IDT
 */
public class Main {
	
	/**
	 * cli key for path to the executable black-box jar to test
	 */
	public static final String JAR_TO_TEST_PATH = "jarToTestPath";
	
	/**
	 * cli key for path to the directory to be used to store output generated by 
	 * jacoco framework
	 */
	public static final String JACOCO_OUTPUT_PATH = "jacocoOutputPath";
	
	/**
	 * cli key for path to the jacoco agent jar used to instrument the executable
	 * black-box jar in order to collect code coverage metrics
	 */
	public static final String JACOCO_AGENT_JAR_PATH = "jacocoAgentJarPath";
	
	/**
	 * cli key for application help
	 */
	public static final String HELP = "help";
	
	/**
	 * alternative cli key for application help
	 */
	public static final String ALT_HELP = "h";
	
	/**
	 * option to enable/disable converting tests to json
	 */
	public static final String NO_CONVERT_TO_JSON = "noJson";
	
	/**
	 * number of exploratory black box tests to run
	 */
	public static final String TEST_ITERATIONS = "bbTests";
	
	/**
	 * test time goal
	 */
	public static final String TEST_TIME = "timeGoal";
	
	/**
	 * only output YAML
	 */
	public static final String ONLY_YAML = "toolChain";
	
	/**
	 * number of threads to use for basic tests
	 */
	public static final String BASIC_TEST_THREADS = "basicThreads";
	
	/**
	 * Entry-point method for the black-box testing framework 
	 * 
	 * @param args - String array of command line arguments
	 */
	public static void main(String[] args) {
		
		CommandLineParser parser = new DefaultParser();
		
		Options options = new Options();
		options.addOption(JAR_TO_TEST_PATH, true, "path to the executable jar to test");
		options.addOption(JACOCO_OUTPUT_PATH, true, "path to directory for jacoco output");
		options.addOption(JACOCO_AGENT_JAR_PATH, true, "path to the jacoco agent jar");
		options.addOption(NO_CONVERT_TO_JSON, false, "disable converting test cases to json");
		options.addOption(TEST_ITERATIONS, true, "number of exploratory black box tests to run (default: 1000 iterations)");
		options.addOption(TEST_TIME, true, "maximum time limit for exploratory black box tests to run (default: 300 seconds)");
		options.addOption(BASIC_TEST_THREADS, true, "number of threads to use for basic tests (default: 5 threads)");
		options.addOption(ONLY_YAML, false, "only output YAML summary");
		options.addOption(HELP, false, "display this help message");
		options.addOption(ALT_HELP, false, "display this help message");
		
		options.getOption(JAR_TO_TEST_PATH).setRequired(true);
		options.getOption(JACOCO_OUTPUT_PATH).setRequired(false);
		options.getOption(JACOCO_AGENT_JAR_PATH).setRequired(false);
		options.getOption(NO_CONVERT_TO_JSON).setRequired(false);
		options.getOption(TEST_TIME).setRequired(false);
		options.getOption(TEST_ITERATIONS).setRequired(false);
		options.getOption(ONLY_YAML).setRequired(false);
		options.getOption(BASIC_TEST_THREADS).setRequired(false);
		
		try {
			CommandLine cliArgs = parser.parse(options, args);
			if (cliArgs != null){
				if (cliArgs.hasOption(JAR_TO_TEST_PATH)) {
					
					String jarToTestPath = cliArgs.getOptionValue(JAR_TO_TEST_PATH);
					File jarToTestFile = new File(jarToTestPath);
					File testFile = new File(jarToTestFile.getParent(), jarToTestFile.getName().replaceFirst("[.][^.]+$", "") + ".json");
					
					String jacocoOutputDirPath;
					if (cliArgs.hasOption(JACOCO_OUTPUT_PATH)) {
						jacocoOutputDirPath = cliArgs.getOptionValue(JACOCO_OUTPUT_PATH);
					}
					else {
						jacocoOutputDirPath = createTempDir().getAbsolutePath();
					}
					String jacocoAgentJarPath = null;
					if (cliArgs.hasOption(JACOCO_AGENT_JAR_PATH)) {
						jacocoAgentJarPath = cliArgs.getOptionValue(JACOCO_AGENT_JAR_PATH);
					}
					else {
						try {
							File tempFile = File.createTempFile("jacocoJar", "agent.jar");
							tempFile.deleteOnExit();
							
							InputStream fileStream = Main.class.getResourceAsStream("agent.jar");
							
							OutputStream out = new FileOutputStream(tempFile);
							byte[] buffer = new byte[1024];
							int len = fileStream.read(buffer);
							while (len != -1) {
								out.write(buffer, 0, len);
								len = fileStream.read(buffer);
							}
							fileStream.close();
							out.close();
							jacocoAgentJarPath = tempFile.getAbsolutePath();
						}
						catch (Exception ex) {
							System.err.println("Error: Unable to extract Jacoco Agent jar!");
							System.err.println("You can use the -" + JACOCO_OUTPUT_PATH + " option to specify the agent file.");
							ex.printStackTrace();
							System.exit(0);
						}
					}
					
					int numThreads = 5;
					if (cliArgs.hasOption(BASIC_TEST_THREADS)) {
						try {
							numThreads = Integer.parseInt(cliArgs.getOptionValue(BASIC_TEST_THREADS));
						}
						catch (NumberFormatException e) {
							numThreads = 5;
						}
					}

					Tester tester = new Tester();
					if (tester.init(jarToTestPath, jacocoOutputDirPath, jacocoAgentJarPath, testFile.getAbsolutePath(), cliArgs.hasOption(NO_CONVERT_TO_JSON))) {
						tester.executeBasicTests(numThreads);
						tester.executeSecurityTests();
						tester.printYaml();
					}
					
				// if the user has requested help
				} else if (cliArgs.hasOption(HELP) || cliArgs.hasOption(ALT_HELP)) {
					
					printHelp(options);
					
			    // user did not request help and we had an inadequate number of arguments
				} else {
					System.out.println("Failed to execute - the -" + JAR_TO_TEST_PATH + " argument is required.");
					printHelp(options);
				}
			}
		
		} catch( ParseException exp ) {
		    System.out.println( "An error occurred during command line parsing: " + exp.getMessage());
		}
	}
	
	private static File createTempDir() {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = "jacocoOutput-" + System.currentTimeMillis() + "-";
		for (int x = 0; x < 10; x++) {
			File tempDir = new File(baseDir, baseName + x);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create temporary directory in " + baseDir.getAbsolutePath() + "!");
	}
	
	/**
	 * private static method used to print the application help
	 */
	private static void printHelp(Options options) {
		 String header = "\n";
		 String footer = "\nFor additional information about the testing framework, please see the documentation provided with this application.";
		 
		 HelpFormatter formatter = new HelpFormatter();
		 formatter.printHelp("com.idtus.contest.winter2017.framework", header, options, footer, true);
	}
	
}
