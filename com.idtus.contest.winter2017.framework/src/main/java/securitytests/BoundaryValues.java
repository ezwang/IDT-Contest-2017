package securitytests;

import java.util.Random;

public class BoundaryValues {
	public static final Integer[] INTS = new Integer[] {
			0, 1, 2, 3, 7, -1, -2,
			Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MAX_VALUE >> 1,
			Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE >> 1
	};
	public static final Double[] DOUBLES = new Double[] {
			0.0, -0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE
	};
	public static final String[] STRINGS = new String[] {
			"", "testing 123", "rich", "\u0001", "\\x00", "\\u0000", "%00", "\ufeff"
	};

	public static Integer pickRandomInteger(Random random) {
		return Util.pickRandomValue(INTS, random);
	}

	public static Double pickRandomDouble(Random random) {
		return Util.pickRandomValue(DOUBLES, random);
	}

	public static String pickRandomString(Random random) {
		return Util.pickRandomValue(STRINGS, random);
	}
}
