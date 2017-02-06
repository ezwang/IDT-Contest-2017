package securitytests;

import java.util.Random;

import contest.winter2017.ParameterType;

public class DefaultSelector implements RandomParameterTest.SingleValueSelector {
	private Random random;

	public DefaultSelector(Random random) {
		this.random = random;
	}

	@Override
	public Object getParameterValue(ParameterType<?> type) {
		if (type instanceof ParameterType.IntegerType) {
			return BoundaryValues.pickRandomInteger(random);
		}
		else if (type instanceof ParameterType.DoubleType) {
			return BoundaryValues.pickRandomDouble(random);
		}
		else if (type instanceof ParameterType.StringType) {
			return BoundaryValues.pickRandomString(random);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
}
