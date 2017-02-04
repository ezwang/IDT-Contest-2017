package contest.winter2017;

public class ParameterType<T> {
	Class<T> clazz;

	private ParameterType(Class<T> clazz) {
		this.clazz = clazz;
	}

	public boolean isInstance(Object variable) {
		return clazz.isInstance(variable);
	}

	public static class StringType extends ParameterType<String> {
		public StringType() {
			super(String.class);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ParameterType)) {
			return false;
		}
		ParameterType<?> pObj = (ParameterType<?>)obj;
		return pObj.clazz == this.clazz;
	}

	public static abstract class NumberType<T extends Number> extends ParameterType<T> {
		private final T min;
		private final T max;

		protected NumberType(Class<T> clazz, T min, T max) {
			super(clazz);
			this.min = min;
			this.max = max;
		}

		/**
		 * Getter for the min value associated with this parameter (if one exists)
		 * @return Object representing the minimum value associated with this parameter
		 */
		public T getMin() {
			return min;
		}

		/**
		 * Getter for the max value associated with this parameter (if one exists)
		 * @return Object representing the maximum value associated with this parameter
		 */
		public T getMax() {
			return max;
		}
	}

	public static class IntegerType extends NumberType<Integer> {
		public IntegerType() {
			super(Integer.class, null, null);
		}
		public IntegerType(Integer min, Integer max) {
			super(Integer.class, min, max);
		}
	}

	public static class DoubleType extends NumberType<Double> {
		public DoubleType() {
			super(Double.class, null, null);
		}
		public DoubleType(Double min, Double max) {
			super(Double.class, min, max);
		}
	}
}