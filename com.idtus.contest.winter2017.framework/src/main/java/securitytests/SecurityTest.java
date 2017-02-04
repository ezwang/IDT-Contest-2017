package securitytests;

import java.util.List;

import contest.winter2017.Parameter;
import contest.winter2017.ParameterFactory;

public interface SecurityTest {
	public List<List<String>> getTests(ParameterFactory parameterFactory);
}
