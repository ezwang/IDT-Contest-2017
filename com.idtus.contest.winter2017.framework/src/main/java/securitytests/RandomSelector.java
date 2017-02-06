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
			char[] arr = new char[random.nextInt(32)];
			for (int i = 0; i < arr.length; i++) {
				// apparently, we can't use null characters
				arr[i] = (char) (random.nextInt(255) + 1);
			}
			return String.valueOf(arr);
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

}
