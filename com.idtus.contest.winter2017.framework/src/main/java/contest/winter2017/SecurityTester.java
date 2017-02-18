package contest.winter2017;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import securitytests.ArgumentAmountTest;
import securitytests.CorruptedInputTest;
import securitytests.RandomParameterTest;

/**
 * Exploratory security vulnerability testing is implemented here.
 * 
 */
public class SecurityTester {
	private final ProgramRunner programRunner;
	private final Random random;

	private Set<String> errorMessages;
	private List<Output> outputs = null;
	
	@SuppressWarnings("unused")
	private int passCount;
	@SuppressWarnings("unused")
	private int failCount;

	public SecurityTester(ProgramRunner programRunner) {
		this.programRunner = programRunner;
		this.random = new SecureRandom();
	}

	public void runTests(ParameterFactory parameterFactory, List<Test> basicTests)
			throws InterruptedException, ExecutionException {
		passCount = 0;
		failCount = 0;
		errorMessages = new HashSet<>();

		List<List<String>> tests = new ArrayList<>();
		int iterations = programRunner.securityTestIterations;

		final ArgumentAmountTest argumentAmountTest = new ArgumentAmountTest(random);
		final CorruptedInputTest corruptedInputTest = new CorruptedInputTest(basicTests, random);
		final RandomParameterTest randomParameterTest = new RandomParameterTest(parameterFactory, random);

		// create test cases
		argumentAmountTest.generateTests(tests, -1);
		corruptedInputTest.generateTests(tests, (iterations - tests.size()) / 3);
		randomParameterTest.generateTests(tests, iterations - tests.size());
		assert tests.size() >= iterations;

		// run tests
		outputs = programRunner.runTests(tests, programRunner.securityTestTime);
		for (Output output : outputs) {
			String stdErrString = output.getStdErrString();
			if (stdErrString != null && isStdErrExceptional(stdErrString)) {
				errorMessages.add(stdErrString.trim());
				failCount++;
			}
			else {
				passCount++;
			}
		}
	}

	private static boolean isStdErrExceptional(String stdErrString) {
		assert stdErrString != null;
		return stdErrString.startsWith("Exception in");
	}


	public void printInfo(boolean verbose) {
		assert outputs != null;
		if (verbose) {
			for (Output output : outputs) {
				Tester.printBasicTestOutput(output);
			}
		}
	}


	public String getYaml() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unique error count: " + this.errorMessages.size() + "\n");
		if (this.errorMessages.isEmpty()) {
			sb.append("Errors seen: []");
		}
		else {
			sb.append("Errors seen:\n");
			for (String errorString : this.errorMessages) {
				errorString = errorString.trim();
				if (errorString.contains("\n")) {
					sb.append("  - |-\n");
					for (String line : errorString.split("\n")) {
						sb.append("  - " + line + "\n");
					}
				}
				else {
					sb.append("  - " + errorString.trim() + "\n");
				}
			}
		}
		return sb.toString();
	}
}
