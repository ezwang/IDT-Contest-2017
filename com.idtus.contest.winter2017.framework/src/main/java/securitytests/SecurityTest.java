package securitytests;

import java.util.List;

import contest.winter2017.ParameterFactory;

public interface SecurityTest {
	public List<String> getNextInput(ParameterFactory parameterFactory);
}
