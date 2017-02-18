package securitytests;

import java.util.List;
import java.util.Random;

import contest.winter2017.Test;

public class CorruptedInputTest implements SecurityTest {

	private final List<Test> basicTests;
	private final Random random;

	public CorruptedInputTest(List<Test> basicTests, Random random) {
		this.basicTests = basicTests;
		this.random = random;
	}

	@Override
	public void generateTests(List<List<String>> list, int maxCount) {
		for (int i = 0; i < maxCount; i++) {
			list.add(getNextInput());
		}
	}

	public List<String> getNextInput() {
		Test test = Util.pickRandomValue(basicTests, random);
		List<String> startParams = test.getParameters();

		// add an extra parameter
		if (random.nextInt(8) == 0) {
			startParams.add(random.nextInt(startParams.size()), BoundaryValues.pickRandomString(random));
		}

		// remove a parameter
		if (random.nextInt(8) == 0 && startParams.size() > 1) {
			startParams.remove(random.nextInt(startParams.size()));
		}

		// corrupt a parameter
		int paramPos = random.nextInt(startParams.size());
		StringBuilder param = new StringBuilder(startParams.get(paramPos));
		if (param.length() == 0) {
			return startParams;
		}

		int nextVal = param.length() / 2;
		
		if (nextVal > 0) {
			int numToCorrupt = Math.abs(random.nextInt(nextVal));
	
			for (int i = 0; i < numToCorrupt; i++) {
				int stringPos = random.nextInt(param.length());
	
				// update, delete, or insert a character at random
				switch (random.nextInt(3)) {
				case 0:
					param.setCharAt(stringPos, Util.generateRandomChar(random));
					break;
				case 1:
					param.deleteCharAt(stringPos);
					break;
				case 2:
					param.insert(stringPos, Util.generateRandomChar(random));
					break;
				}
			}
		}

		startParams.set(paramPos, param.toString());
		return startParams;
	}

}
