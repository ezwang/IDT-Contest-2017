package securitytests;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import contest.winter2017.ParameterFactory;

public class ArgumentAmountTest implements SecurityTest {
	private Random random;

	public ArgumentAmountTest(Random random) {
		this.random = random;
	}

	@Override
	public List<String> getNextInput(ParameterFactory parameterFactory) {
		int len = random.nextInt(1000);
		return Stream.generate(this::pickString)
				.limit(len)
				.map(String::valueOf)
				.collect(Collectors.toList());
	}

	private String pickString() {
		// TODO: use other strings?
		return BoundaryValues.pickRandomString(random);
	}
}
