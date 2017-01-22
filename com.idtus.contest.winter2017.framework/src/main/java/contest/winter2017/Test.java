package contest.winter2017;

import java.util.List;

/**
 * Class that represents a basic test that is extracted from the executable jar and then run against the executable jar
 * to determine a pass/fail status. 
 * 
 * @author IDT
 */
public class Test {
	
	/**
	 * List of parameter values that will be passed into the executable jar as 
	 * as single test
	 */
	private List<String> parameters;
	
	/**
	 * The regex string that describes the expected std out result for the test
	 */
	private String stdOutExpectedResultRegex;
	
	/**
	 * The regex string that describes the expected std err result for the test
	 */
	private String stdErrExpectedResultRegex;


	/**
	 * Ctr for Test object
	 */
	public Test(List<String> parameters,
			String stdOutExpectedResultRegex,
			String stdErrExpectedResultRegex) {
		this.parameters = parameters;
		this.stdOutExpectedResultRegex = stdOutExpectedResultRegex;
		this.stdErrExpectedResultRegex = stdErrExpectedResultRegex;
	}


	/**
	 * Getter for parameters List
	 * @return
	 */
	public List<String> getParameters() {
		return parameters;
	}

	
	/**
	 * Getter for Standard Out expected results regex
	 * @return
	 */
	public String getStdOutExpectedResultRegex() {
		return stdOutExpectedResultRegex;
	}

	
	/**
	 * Getter for Standard Error expected results regex
	 * @return
	 */
	public String getStdErrExpectedResultRegex() {
		return stdErrExpectedResultRegex;
	}
	
}
