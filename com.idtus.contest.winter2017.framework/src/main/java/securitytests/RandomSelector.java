package securitytests;

import java.util.Random;

import contest.winter2017.ParameterType;

public class RandomSelector implements RandomParameterTest.SingleValueSelector {
	private Random random;

	public RandomSelector(Random random) {
		this.random = random;
	}

	@Override
	public Object getParameterValue(ParameterType<?> type) {
		if (type instanceof ParameterType.IntegerType) {
			return random.nextInt();
		}
		else if (type instanceof ParameterType.DoubleType) {
			return random.nextDouble();
		}
		else if (type instanceof ParameterType.StringType) {
			return Util.generateRandomString(32, random);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

}
