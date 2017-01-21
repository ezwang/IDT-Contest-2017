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

	private Output output;
	private Set<String> yaml_errors;

	public SecurityTester(ProgramRunner programRunner) {
		this.programRunner = programRunner;
		this.yaml_errors = new HashSet<String>();
	}

	@SuppressWarnings("rawtypes")
	public void runTests(ParameterFactory parameterFactory) {
		/////////// START EXAMPLE CODE /////////////

		// This example demonstrates how to use the ParameterFactory to figure out the parameter types of parameters
		// for each of the jars under test - this can be a difficult task because of the concepts of fixed and
		// dependent parameters (see the explanation at the top of the ParameterFactory class). As we figure out 
		// what each parameter type is, we are assigning it a simple (dumb) value so that we can use those parameters 
		// to execute the black-box jar. By the time we finish this example, we will have an array of concrete 
		// parameters that we can use to execute the black-box jar.
		List<String> previousParameterStrings = new ArrayList<String>(); // start with a blank parameter list since we are going to start with the first parameter
		List<Parameter> potentialParameters = parameterFactory.getNext(previousParameterStrings);
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
			potentialParameters = parameterFactory.getNext(previousParameterStrings);
		}

		// This example demonstrates how to execute the black-box jar with concrete parameters
		// and how to access (print to screen) the standard output and error from the run
		output = programRunner.instrumentAndExecuteCode(previousParameterStrings);

		if (!output.getStdErrString().isEmpty()) {
			String err = output.getStdErrString().trim();
			this.yaml_errors.add(err);
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
		sb.append("Unique error count: " + this.yaml_errors.size() + "\n");
		if (this.yaml_errors.isEmpty()) {
			sb.append("Errors seen: []");
		}
		else {
			sb.append("Errors seen:\n");
			for (String errorString : this.yaml_errors) {
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
