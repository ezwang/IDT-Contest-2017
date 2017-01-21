package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * This is the half of the framework that IDT has completed. We are able to pull basic tests 
 * directly from the executable jar. We are able to run the tests and assess the output as PASS/FAIL.
 * 
 * You likely do not have to change this part of the framework. We are considering this complete and 
 * want your team to focus more on the SecurityTests. 
 */
public class BasicTester {
	private final ProgramRunner programRunner;

	private int passCount;
	private int failCount;
	private List<BasicTestResult> results;

	public BasicTester(ProgramRunner programRunner) {
		this.programRunner = programRunner;
	}

	public void runTests(List<Test> tests) throws InterruptedException, ExecutionException {
		passCount = 0;
		failCount = 0;

		// create list of parameters for each test
		List<List<String>> testParametersList = new ArrayList<List<String>>();
		for (Test test : tests) {
			List<String> parameters = new ArrayList<String>();
			for (Object o : test.getParameters()) {
				parameters.add(o.toString());
			}
			testParametersList.add(parameters);
		}

		// collect results in list
		List<Output> outputs = programRunner.runTests(testParametersList);
		assert tests.size() == outputs.size();

		results = new ArrayList<BasicTestResult>();
		for (int i = 0; i < outputs.size(); i++) {
			BasicTestResult result = getBasicTestResult(tests.get(i), outputs.get(i));
			if (result.passed) {
				passCount++;
			} else {
				failCount++;
			}
			results.add(result);
		}
	}


	public void printInfo(boolean verbose) {
		assert results != null;
		for (BasicTestResult result : results) {
			if (verbose) {
				Tester.printBasicTestOutput(result.output);
			}
			if (!result.passed) {
				System.out.println("Test Failed!");
				System.out.println("\t -> parameters: " + result.parameters);
				System.out.println(result.error);
				System.out.println(Tester.HORIZONTAL_LINE);
			}
		}
		System.out.printf("basic test results: %d total, %d pass, %d fail%n",
				results.size(), passCount, failCount);
		System.out.println(Tester.HORIZONTAL_LINE);
	}


	public String getYaml() {
		assert results != null;
		String formatString = "Total predefined tests run: %d%n" +
				"Number of predefined tests that passed: %d%n" +
				"Number of predefined tests that failed: %d";
		return String.format(formatString, results.size(), passCount, failCount);
	}


	private static BasicTestResult getBasicTestResult(Test test, Output output) {
		BasicTestResult result = new BasicTestResult();
		result.parameters = test.getParameters().toString();
		result.output = output;

		String pOut = output.getStdOutString().replaceAll("[\\r\\n]", "");
		String pErr = output.getStdErrString().replaceAll("[\\r\\n]", "");

		// NOTE: We also need to check with newlines removed for backwards compatibility.
		String pOutAlt = pOut.replaceAll("[\\r\\n]", "");
		String pErrAlt = pErr.replaceAll("[\\r\\n]", "");

		// determine the result of the test based on expected output/error regex
		boolean passedOut = pOut.matches(test.getStdOutExpectedResultRegex()) ||
				pOutAlt.matches(test.getStdErrExpectedResultRegex());
		boolean passedErr = pErr.matches(test.getStdErrExpectedResultRegex()) ||
				pErrAlt.matches(test.getStdErrExpectedResultRegex());
		result.passed = passedOut && passedErr;

		StringBuilder errorString = new StringBuilder();

		// since we have a failed basic test, show the expectation for the stdout
		if (!passedOut) {
			errorString.append("\t -> stdout: ")
				.append(output.getStdOutString()).append("\n");
			errorString.append("\t -> did not match expected stdout regex: ")
				.append(test.getStdOutExpectedResultRegex()).append("\n");
		}

		// since we have a failed basic test, show the expectation for the stderr
		if (!passedErr) {
			errorString.append("\t -> stderr: ")
				.append(output.getStdErrString()).append("\n");
			errorString.append("\t -> did not match expected stderr regex: ")
				.append(test.getStdErrExpectedResultRegex()).append("\n");
		}

		result.error = errorString.toString();
		return result;
	}


	private static class BasicTestResult {
		public String parameters;
		public Output output;
		public boolean passed;
		public String error;
	}
}
