package securitytests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import contest.winter2017.Parameter;
import contest.winter2017.ParameterFactory;
import contest.winter2017.ParameterType;

/**
 * Generates random test inputs by randomly picking possible parameters, and using a 
 * SingleValueSelector to select necessary values for each ParameterType.
 */
public class RandomParameterTest implements SecurityTest {
	private Random random;
	private SingleValueSelector[] selectors;

	public RandomParameterTest(Random random) {
		this.random = random;
		this.selectors = new SingleValueSelector[] {
				new DefaultSelector(random),
				new MismatchedTypeSelector(random),
				new RandomSelector(random)
		};
	}

	@Override
	public List<String> getNextInput(ParameterFactory parameterFactory) {
		List<String> previousParameterStrings = new ArrayList<String>();
		List<Parameter> potentialParameters = parameterFactory.getNext(previousParameterStrings);

		while (!potentialParameters.isEmpty()) {
			// pick one of the possible parameters at random
			int index = random.nextInt(potentialParameters.size());
			Parameter potentialParameter = potentialParameters.get(index);

			// if the parameter is optional, try not using it
			if (potentialParameter.isOptional() && random.nextBoolean()) {
				break;
			}

			// choose values for each format object we need to fill in
			List<String> formatVariableValues = potentialParameter.getTypeList().stream()
					.map(this::chooseParameterValue)
					.map(String::valueOf)
					.collect(Collectors.toList());

			// build the formatted parameter string with the chosen values
			String parameterString = potentialParameter.formatParameterFromStrings(formatVariableValues);
			previousParameterStrings.add(parameterString);

			// get next list of possible parameters
			potentialParameters = parameterFactory.getNext(previousParameterStrings);
		}

		return previousParameterStrings;
	}

	private Object chooseParameterValue(ParameterType<?> type) {
		// TODO: possibly weight selectors
		int index = random.nextInt(selectors.length);
		return selectors[index].getParameterValue(type);
	}

	public interface SingleValueSelector {
		public Object getParameterValue(ParameterType<?> type);
	}
}
