package contest.winter2017;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import securitytests.ArgumentAmountTest;
import securitytests.DefaultTest;
import securitytests.TypeTest;

/**
 * Exploratory security vulnerability testing is implemented here.
 * 
 */
public class SecurityTester {
	private final ProgramRunner programRunner;

	private Set<String> errorMessages;
	private List<Output> outputs;

	public SecurityTester(ProgramRunner programRunner) {
		this.errorMessages = new HashSet<String>();
		this.programRunner = programRunner;
	}

	public void runTests(ParameterFactory parameterFactory) throws InterruptedException, ExecutionException {
		List<List<String>> tests = new ArrayList<List<String>>();
		
		tests.addAll((new DefaultTest()).getTests(parameterFactory));
		tests.addAll((new TypeTest()).getTests(parameterFactory));
		tests.addAll((new ArgumentAmountTest()).getTests(parameterFactory));
		
		outputs = programRunner.runTests(tests);

		for (Output output : outputs) {
			if (!output.getStdErrString().isEmpty()) {
				String err = output.getStdErrString().trim();
				errorMessages.add(err);
			}
		}
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
