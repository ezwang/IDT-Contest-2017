package securitytests;

import java.util.ArrayList;
import java.util.List;

import contest.winter2017.ParameterFactory;

public class ArgumentAmountTest implements SecurityTest {

	@Override
	public List<List<String>> getTests(ParameterFactory parameterFactory) {
		List<List<String>> out = new ArrayList<List<String>>();
		out.add(new ArrayList<String>());
		List<String> max = new ArrayList<String>(1000);
		for (int i = 0; i < 1000; i++) {
			max.add("test");
		}
		return out;
	}

}
