package contest.winter2017;

import java.util.concurrent.Callable;

class BasicTestRunner implements Callable<BasicTestResult> {
	private final Tester tester;
	private final Test test;

	public BasicTestRunner(Tester tester, Test test) {
		this.tester = tester;
		this.test = test;
	}

	@Override
	public BasicTestResult call() {
		// instrument the code to code coverage metrics, execute the test with given parameters, then show the output
		Output output = this.tester.instrumentAndExecuteCode(test.getParameters().toArray());
		this.tester.printBasicTestOutput(output);

		BasicTestResult result = new BasicTestResult();
		result.parameters = test.getParameters().toString();

		String pOut = output.getStdOutString().trim();
		String pErr = output.getStdErrString().trim();

		// determine the result of the test based on expected output/error regex
		if(pOut.matches(test.getStdOutExpectedResultRegex())
				&& pErr.matches(test.getStdErrExpectedResultRegex())) {
			result.passed = true;
		}
		else {
			result.passed = false;
			// since we have a failed basic test, show the expectation for the stdout
			if(!pOut.matches(test.getStdOutExpectedResultRegex())) {
				result.error = "\t -> stdout: "+output.getStdOutString() + "\n" + "\t ->did not match expected stdout regex: " + test.getStdOutExpectedResultRegex();
			}

			// since we have a failed basic test, show the expectation for the stderr
			if(!pErr.matches(test.getStdErrExpectedResultRegex())) {
				result.error = "\t -> stderr: "+output.getStdErrString() + "\n" + "\t ->did not match expected stderr regex: "+test.getStdErrExpectedResultRegex();
			}
		}
		return result;
	}
}