package securitytests;

import java.util.Random;

import contest.winter2017.ParameterType;

public class MismatchedTypeSelector implements RandomParameterTest.SingleValueSelector {
	private Random random;

	public MismatchedTypeSelector(Random random) {
		this.random = random;
	}

	@Override
	public Object getParameterValue(ParameterType<?> type) {
		int val = random.nextInt(3);
		switch (val) {
		case 0:
			return BoundaryValues.pickRandomInteger(random);
		case 1:
			return BoundaryValues.pickRandomDouble(random);
		case 2:
			return BoundaryValues.pickRandomString(random);
		default:
			throw new AssertionError();
		}
	}
}
