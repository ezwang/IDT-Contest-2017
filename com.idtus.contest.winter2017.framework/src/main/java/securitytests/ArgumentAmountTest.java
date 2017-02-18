package securitytests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgumentAmountTest implements SecurityTest {
	private Random random;
	private MismatchedTypeSelector selector;

	public ArgumentAmountTest(Random random) {
		this.random = random;
		this.selector = new MismatchedTypeSelector(random);
	}

	@Override
	public void generateTests(List<List<String>> list, int maxCount) {
		list.add(new ArrayList<String>());  // empty
		for (int i = 0; i < 2; i++) {
			list.add(getUnicodeTest());
		}
		for (int i = 0; i < 3; i++) {
			list.add(getBoundaryTest());
		}
	}

	public List<String> getUnicodeTest() {
		int len = random.nextInt(1000);
		return Stream.generate(() -> Util.generateBadRandomString(32, random))
				.limit(len).collect(Collectors.toList());
	}

	public List<String> getBoundaryTest() {
		int len = random.nextInt(1000);
		return Stream.generate(this::pickValue)
				.limit(len)
				.map(String::valueOf)
				.collect(Collectors.toList());
	}

	private Object pickValue() {
		// abuse MismatchedTypeSelector, which ignores parameter type
		return selector.getParameterValue(null);
	}
}
