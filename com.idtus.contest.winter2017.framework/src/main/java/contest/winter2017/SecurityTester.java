package contest.winter2017;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the half of the framework that IDT has not completed. We want you to implement your exploratory 
 * security vulnerability testing here.
 * 
 * In an effort to demonstrate some of the features of the framework that you can already utilize, we have
 * provided some example code in the method. The examples only demonstrate how to use existing functionality. 
 */
public class SecurityTester {
	private final ProgramRunner programRunner;

	private Set<String> errorMessages;
	private Output output;

	public SecurityTester(ProgramRunner programRunner) {
		this.errorMessages = new HashSet<String>();
		this.programRunner = programRunner;
	}

	public void runTests(ParameterFactory parameterFactory) {
		/////////// START EXAMPLE CODE /////////////

		// This example demonstrates how to use the ParameterFactory to figure out the parameter types of parameters
		// for each of the jars under test - this can be a difficult task because of the concepts of fixed and
		// dependent parameters (see the explanation at the top of the ParameterFactory class). As we figure out 
		// what each parameter type is, we are assigning it a simple (dumb) value so that we can use those parameters 
		// to execute the black-box jar. By the time we finish this example, we will have an array of concrete 
		// parameters that we can use to execute the black-box jar.

		// start with a blank parameter list since we are going to start with the first parameter
		List<String> previousParameterStrings = new ArrayList<String>();
		List<Parameter> potentialParameters = parameterFactory.getNext(previousParameterStrings);

		while (!potentialParameters.isEmpty()) {
			Parameter potentialParameter = potentialParameters.get(0);

			// TODO? - your team might want to look at this flag and handle it as well!
			// if (potentialParameter.isOptional())

			// loop over the areas of the format that must be replaced and choose values
			List<Object> formatVariableValues = new ArrayList<Object>();
			for (ParameterType<?> type : potentialParameter.getTypeList()) {
				if (type instanceof ParameterType.IntegerType) {
					// dumb logic - always use '1' for an Integer
					formatVariableValues.add(new Integer(1));
				}
				else if (type instanceof ParameterType.DoubleType) {
					// dumb logic - always use '1.0' for a Double
					formatVariableValues.add(new Double(1.0));
				}
				else if (type instanceof ParameterType.StringType) {
					// dumb logic - always use 'one' for a String
					formatVariableValues.add(new String("one"));
				}
				else {
					formatVariableValues.add("unknown type");
				}
			}

			// build the formatted parameter string with the chosen values (eg. 1:1PM EST)
			String parameterString = potentialParameter.getFormattedParameter(formatVariableValues);
			previousParameterStrings.add(parameterString);

			// because of the challenge associated with dependent parameters, we must go one parameter
			// at a time, building up the parameter list - getNext is the method that we are using 
			// to get the next set of options, given an accumulating parameter list. 
			potentialParameters = parameterFactory.getNext(previousParameterStrings);
		}

		// This example demonstrates how to execute the black-box jar with concrete parameters
		// and how to access (print to screen) the standard output and error from the run
		output = programRunner.instrumentAndExecuteCode(previousParameterStrings);

		if (!output.getStdErrString().isEmpty()) {
			String err = output.getStdErrString().trim();
			errorMessages.add(err);
		}

		/////////// END EXAMPLE CODE ////////////// 
	}


	public void printInfo(boolean verbose) {
		assert output != null;
		if (verbose) {
			Tester.printBasicTestOutput(output);
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
